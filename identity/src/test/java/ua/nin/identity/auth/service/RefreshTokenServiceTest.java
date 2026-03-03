package ua.nin.identity.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ua.nin.identity.auth.dto.IssueNewResult;
import ua.nin.identity.auth.dto.RefreshIssueResult;
import ua.nin.identity.auth.exception.exceptions.InvalidRefreshTokenException;
import ua.nin.identity.auth.exception.exceptions.RefreshReuseDetectedException;
import ua.nin.identity.auth.model.RefreshToken;
import ua.nin.identity.auth.model.RefreshTokenFamily;
import ua.nin.identity.auth.model.User;
import ua.nin.identity.auth.repository.RefreshTokenFamilyRepository;
import ua.nin.identity.auth.repository.RefreshTokenRepository;
import ua.nin.identity.auth.util.TimeTokenUtils;

import java.net.InetAddress;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private RefreshTokenFamilyRepository familyRepository;
    @Mock
    private TimeTokenUtils timeTokenUtils;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    void issueNew_createsFamilyAndToken() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTtlDays", 7L);
        User user = User.builder().id(11L).build();

        when(timeTokenUtils.generateRawToken()).thenReturn("raw-token");
        when(timeTokenUtils.hash("raw-token")).thenReturn("hash-token");
        when(familyRepository.save(any(RefreshTokenFamily.class)))
                .thenAnswer(inv -> {
                    RefreshTokenFamily fam = inv.getArgument(0);
                    fam.setId(55L);
                    return fam;
                });

        IssueNewResult result = refreshTokenService.issueNew(user, "ua", "127.0.0.1");

        assertEquals("raw-token", result.rawRefreshToken());
        assertEquals(55L, result.familyId());

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());
        assertEquals("hash-token", tokenCaptor.getValue().getTokenHash());
        assertEquals(55L, tokenCaptor.getValue().getFamily().getId());
        assertEquals(user, tokenCaptor.getValue().getUser());
        assertTrue(tokenCaptor.getValue().getExpiresAt().isAfter(Instant.now().plus(6, ChronoUnit.DAYS)));
    }

    @Test
    void rotate_missingToken_throws() {
        assertThatThrownBy(() -> refreshTokenService.rotate(" ", "ua", "ip"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("Missing refresh token");
    }

    @Test
    void rotate_familyRevoked_throws() {
        when(timeTokenUtils.hash("raw")).thenReturn("hash");
        RefreshToken token = RefreshToken.builder()
                .family(RefreshTokenFamily.builder().id(10L).build())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .build();
        when(refreshTokenRepository.findByTokenHash("hash")).thenReturn(Optional.of(token));

        RefreshTokenFamily family = RefreshTokenFamily.builder()
                .id(10L)
                .revokedAt(Instant.now())
                .build();
        when(familyRepository.findById(10L)).thenReturn(Optional.of(family));

        assertThatThrownBy(() -> refreshTokenService.rotate("raw", "ua", "ip"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("Family revoked");
    }

    @Test
    void rotate_expiredToken_revokesFamily() {
        when(timeTokenUtils.hash("raw")).thenReturn("hash");
        RefreshToken token = RefreshToken.builder()
                .family(RefreshTokenFamily.builder().id(10L).build())
                .issuedAt(Instant.now().minus(10, ChronoUnit.DAYS))
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();
        when(refreshTokenRepository.findByTokenHash("hash")).thenReturn(Optional.of(token));

        RefreshTokenFamily family = RefreshTokenFamily.builder()
                .id(10L)
                .build();
        when(familyRepository.findById(10L)).thenReturn(Optional.of(family));

        assertThatThrownBy(() -> refreshTokenService.rotate("raw", "ua", "ip"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("expired");

        verify(familyRepository).revokeFamily(eq(10L), any(Instant.class));
        verify(refreshTokenRepository).revokeAllInFamily(eq(10L), any(Instant.class));
    }

    @Test
    void rotate_reuseDetected_revokesFamily() {
        when(timeTokenUtils.hash("raw")).thenReturn("hash");
        RefreshToken token = RefreshToken.builder()
                .family(RefreshTokenFamily.builder().id(10L).build())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .revokedAt(Instant.now())
                .build();
        when(refreshTokenRepository.findByTokenHash("hash")).thenReturn(Optional.of(token));

        RefreshTokenFamily family = RefreshTokenFamily.builder()
                .id(10L)
                .build();
        when(familyRepository.findById(10L)).thenReturn(Optional.of(family));

        assertThatThrownBy(() -> refreshTokenService.rotate("raw", "ua", "ip"))
                .isInstanceOf(RefreshReuseDetectedException.class)
                .hasMessageContaining("reuse detected");

        verify(familyRepository).revokeFamily(eq(10L), any(Instant.class));
        verify(refreshTokenRepository).revokeAllInFamily(eq(10L), any(Instant.class));
    }

    @Test
    void rotate_success_createsNextToken() throws Exception {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTtlDays", 14L);
        when(timeTokenUtils.hash("raw")).thenReturn("hash");
        when(timeTokenUtils.generateRawToken()).thenReturn("new-raw");
        when(timeTokenUtils.hash("new-raw")).thenReturn("new-hash");

        User user = User.builder().id(11L).build();
        RefreshToken token = RefreshToken.builder()
                .id(2L)
                .user(user)
                .family(RefreshTokenFamily.builder().id(10L).build())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .build();
        when(refreshTokenRepository.findByTokenHash("hash")).thenReturn(Optional.of(token));

        RefreshTokenFamily family = RefreshTokenFamily.builder()
                .id(10L)
                .ip(InetAddress.getByName("127.0.0.1"))
                .build();
        when(familyRepository.findById(10L)).thenReturn(Optional.of(family));

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(inv -> {
                    RefreshToken saved = inv.getArgument(0);
                    if (saved.getId() == null) {
                        saved.setId(99L);
                    }
                    return saved;
                });

        RefreshIssueResult result = refreshTokenService.rotate("raw", "ua", "127.0.0.1");

        assertEquals("new-raw", result.rawRefreshToken());
        assertEquals(10L, result.familyId());

        verify(familyRepository).touch(eq(10L), any(Instant.class), eq("ua"), any());
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    void revokeByRefresh_missingToken_noop() {
        refreshTokenService.revokeByRefresh(null);

        verifyNoInteractions(refreshTokenRepository, familyRepository);
    }

    @Test
    void revokeByRefresh_existingToken_revokesFamily() {
        when(timeTokenUtils.hash("raw")).thenReturn("hash");
        RefreshToken token = RefreshToken.builder().family(RefreshTokenFamily.builder().id(5L).build()).build();
        when(refreshTokenRepository.findByTokenHash("hash")).thenReturn(Optional.of(token));

        refreshTokenService.revokeByRefresh("raw");

        verify(familyRepository).revokeFamily(eq(5L), any(Instant.class));
        verify(refreshTokenRepository).revokeAllInFamily(eq(5L), any(Instant.class));
    }

    @Test
    void revokeAllForUser_callsRepositories() {
        refreshTokenService.revokeAllForUser(10L);

        verify(familyRepository).revokeAllForUser(eq(10L), any(Instant.class));
        verify(refreshTokenRepository).revokeAllForUser(eq(10L), any(Instant.class));
    }
}
