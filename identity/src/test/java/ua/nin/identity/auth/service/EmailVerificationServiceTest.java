package ua.nin.identity.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ua.nin.identity.auth.exception.exceptions.InvalidTokenException;
import ua.nin.identity.auth.model.EmailVerificationToken;
import ua.nin.identity.auth.model.Status;
import ua.nin.identity.auth.model.User;
import ua.nin.identity.auth.repository.EmailVerificationTokenRepository;
import ua.nin.identity.auth.repository.UserRepository;
import ua.nin.identity.auth.util.TimeTokenUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailVerificationTokenRepository tokenRepository;
    @Mock
    private TimeTokenUtils timeTokenUtils;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    @Test
    void verify_missingToken_throws() {
        assertThatThrownBy(() -> emailVerificationService.verify(""))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Missing verification token");
    }

    @Test
    void verify_expiredToken_throws() {
        when(timeTokenUtils.hash("raw")).thenReturn("hash");
        EmailVerificationToken token = EmailVerificationToken.builder()
                .tokenHash("hash")
                .expiresAt(Instant.now().minus(5, ChronoUnit.MINUTES))
                .build();
        when(tokenRepository.findByTokenHash("hash")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> emailVerificationService.verify("raw"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void verify_success_updatesUserAndToken() {
        when(timeTokenUtils.hash("raw")).thenReturn("hash");

        User user = User.builder().id(9L).status(Status.PENDING_EMAIL).build();
        EmailVerificationToken token = EmailVerificationToken.builder()
                .tokenHash("hash")
                .user(user)
                .expiresAt(Instant.now().plus(5, ChronoUnit.MINUTES))
                .build();

        when(tokenRepository.findByTokenHash("hash")).thenReturn(Optional.of(token));

        emailVerificationService.verify("raw");

        verify(userRepository).save(user);
        ArgumentCaptor<EmailVerificationToken> tokenCaptor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        assertEquals(Status.ACTIVE, user.getStatus());
        assertEquals(token, tokenCaptor.getValue());
    }

    @Test
    void send_generatesToken() {
        ReflectionTestUtils.setField(emailVerificationService, "verifyTtlMinutes", 30L);
        when(timeTokenUtils.generateRawToken()).thenReturn("raw");
        when(timeTokenUtils.hash("raw")).thenReturn("hash");

        User user = User.builder().id(1L).email("user@site.com").build();

        emailVerificationService.send(user);

        ArgumentCaptor<EmailVerificationToken> tokenCaptor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        assertEquals("hash", tokenCaptor.getValue().getTokenHash());
    }

    @Test
    void resend_skipsWhenUserMissing() {
        when(userRepository.findByEmail("user@site.com")).thenReturn(Optional.empty());

        emailVerificationService.resend("user@site.com");

        verifyNoInteractions(tokenRepository);
    }

    @Test
    void resend_createsTokenWhenPending() {
        ReflectionTestUtils.setField(emailVerificationService, "verifyTtlMinutes", 60L);
        when(timeTokenUtils.generateRawToken()).thenReturn("raw");
        when(timeTokenUtils.hash("raw")).thenReturn("hash");

        User user = User.builder().id(1L).email("user@site.com").status(Status.PENDING_EMAIL).build();
        when(userRepository.findByEmail("user@site.com")).thenReturn(Optional.of(user));

        emailVerificationService.resend("user@site.com");

        verify(tokenRepository).save(any(EmailVerificationToken.class));
    }
}
