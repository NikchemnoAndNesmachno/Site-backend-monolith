package ua.nin.identity.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import ua.nin.identity.auth.exception.exceptions.BadCredentialsException;
import ua.nin.identity.auth.exception.exceptions.InvalidTokenException;
import ua.nin.identity.auth.model.Credential;
import ua.nin.identity.auth.model.PasswordResetToken;
import ua.nin.identity.auth.model.Status;
import ua.nin.identity.auth.model.User;
import ua.nin.identity.auth.repository.CredentialRepository;
import ua.nin.identity.auth.repository.PasswordResetTokenRepository;
import ua.nin.identity.auth.repository.UserRepository;
import ua.nin.identity.auth.util.TimeTokenUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CredentialRepository credentialRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TimeTokenUtils timeTokenUtils;
    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private PasswordService passwordService;

    @Test
    void forgot_missingUser_noop() {
        when(userRepository.findByEmail("user@site.com")).thenReturn(Optional.empty());

        passwordService.forgot("user@site.com");

        verifyNoInteractions(passwordResetTokenRepository);
    }

    @Test
    void forgot_inactiveUser_noop() {
        User user = User.builder().id(1L).email("user@site.com").status(Status.BANNED).build();
        when(userRepository.findByEmail("user@site.com")).thenReturn(Optional.of(user));

        passwordService.forgot("user@site.com");

        verifyNoInteractions(passwordResetTokenRepository);
    }

    @Test
    void forgot_createsToken() {
        ReflectionTestUtils.setField(passwordService, "resetTtlMinutes", 30L);
        when(timeTokenUtils.generateRawToken()).thenReturn("raw");
        when(timeTokenUtils.hash("raw")).thenReturn("hash");

        User user = User.builder().id(1L).email("user@site.com").status(Status.ACTIVE).build();
        when(userRepository.findByEmail("user@site.com")).thenReturn(Optional.of(user));

        passwordService.forgot("user@site.com");

        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(captor.capture());
        assertEquals("hash", captor.getValue().getTokenHash());
    }

    @Test
    void reset_missingToken_throws() {
        assertThatThrownBy(() -> passwordService.reset("", "newPass"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Missing reset token");
    }

    @Test
    void reset_expiredToken_throws() {
        when(timeTokenUtils.hash("raw")).thenReturn("hash");
        PasswordResetToken token = PasswordResetToken.builder()
                .tokenHash("hash")
                .expiresAt(Instant.now().minus(2, ChronoUnit.MINUTES))
                .build();
        when(passwordResetTokenRepository.findByTokenHash("hash")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> passwordService.reset("raw", "new"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void reset_success_updatesPasswordAndRevokesTokens() {
        when(timeTokenUtils.hash("raw")).thenReturn("hash");

        User user = User.builder().id(10L).build();
        PasswordResetToken token = PasswordResetToken.builder()
                .tokenHash("hash")
                .user(user)
                .expiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                .build();
        Credential credential = Credential.builder()
                .user(user)
                .userId(10L)
                .passwordHash("old")
                .failedLoginAttempts(2)
                .build();

        when(passwordResetTokenRepository.findByTokenHash("hash")).thenReturn(Optional.of(token));
        when(credentialRepository.findById(10L)).thenReturn(Optional.of(credential));
        when(passwordEncoder.encode("newPass")).thenReturn("encoded");

        passwordService.reset("raw", "newPass");

        assertEquals("encoded", credential.getPasswordHash());
        assertEquals(0, credential.getFailedLoginAttempts());
        verify(refreshTokenService).revokeAllForUser(10L);
        verify(passwordResetTokenRepository).save(token);
    }

    @Test
    void change_invalidPassword_throws() {
        long userId = 99L;

        Credential credential = Credential.builder()
                .userId(userId)
                .passwordHash("old")
                .build();
        when(credentialRepository.findById(99L)).thenReturn(Optional.of(credential));
        when(passwordEncoder.matches("bad", "old")).thenReturn(false);

        assertThatThrownBy(() -> passwordService.change(userId, "bad", "new"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Current password invalid");
    }

    @Test
    void change_success_updatesPasswordAndRevokesTokens() {
        long userId = 99L;

        Credential credential = Credential.builder()
                .userId(userId)
                .passwordHash("old")
                .build();
        when(credentialRepository.findById(99L)).thenReturn(Optional.of(credential));
        when(passwordEncoder.matches("oldPass", "old")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("encoded");

        passwordService.change(userId,"oldPass", "newPass");

        assertEquals("encoded", credential.getPasswordHash());
        verify(refreshTokenService).revokeAllForUser(userId);
    }
}
