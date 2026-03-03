package ua.nin.identity.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.nin.identity.auth.dto.IssueNewResult;
import ua.nin.identity.auth.dto.RefreshIssueResult;
import ua.nin.identity.auth.exception.exceptions.InvalidRefreshTokenException;
import ua.nin.identity.auth.exception.exceptions.RefreshReuseDetectedException;
import ua.nin.identity.auth.model.RefreshToken;
import ua.nin.identity.auth.model.RefreshTokenFamily;
import ua.nin.identity.auth.model.User;
import ua.nin.identity.auth.repository.RefreshTokenFamilyRepository;
import ua.nin.identity.auth.repository.RefreshTokenRepository;
import ua.nin.identity.auth.util.InetAddressUtils;
import ua.nin.identity.auth.util.TimeTokenUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static ua.nin.common.util.StringHelperUtils.normalizeAndTruncate;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenFamilyRepository familyRepository;
    private final TimeTokenUtils timeTokenUtils;

    @Value("${security.refresh.ttl-days:14}")
    private long refreshTtlDays;

    @Transactional(readOnly = true)
    public User getUserByFamily(long familyId) {
        return familyRepository.findById(familyId)
                .orElseThrow(() -> new InvalidRefreshTokenException("Family not found"))
                .getUser();
    }

    /**
     * Створює нову "сесію/пристрій" (family) + refresh токен.
     * Викликається на LOGIN (або при першій авторизації з пристрою).
     */
    @Transactional
    public IssueNewResult issueNew(User user, String userAgent, String ip) {
        Instant now = Instant.now();

        RefreshTokenFamily family = RefreshTokenFamily.builder()
                .user(user)
                .userAgent(normalizeAndTruncate(userAgent, 512))
                .ip(InetAddressUtils.parseIp(ip))
                .build();

        family = familyRepository.save(family);

        String raw = timeTokenUtils.generateRawToken();
        String hash = timeTokenUtils.hash(raw);

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .family(family)
                .tokenHash(hash)
                .issuedAt(now)
                .expiresAt(now.plus(refreshTtlDays, ChronoUnit.DAYS))
                .build();

        refreshTokenRepository.save(token);

        return new IssueNewResult(raw, family.getId());
    }

    /**
     * ROTATION:
     * - перевіряємо raw refresh (по hash)
     * - якщо токен вже revoked/replaced -> REUSE DETECTED -> revoke всю family
     * - якщо ок -> позначаємо старий як replaced_by, створюємо новий refresh
     */
    @Transactional
    public RefreshIssueResult rotate(String rawRefreshToken, String userAgent, String ip) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new InvalidRefreshTokenException("Missing refresh token");
        }

        Instant now = Instant.now();
        String hash = timeTokenUtils.hash(rawRefreshToken);

        RefreshToken current = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not found"));

        // 1) family must be active
        RefreshTokenFamily family = familyRepository.findById(current.getFamily().getId())
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh family not found"));

        if (family.isRevoked()) {
            throw new InvalidRefreshTokenException("Family revoked");
        }

        // 2) expiry
        if (current.isExpired(now)) {
            // можна ревокнути токен або навіть family
            revokeFamilyInternal(family.getId(), now);
            throw new InvalidRefreshTokenException("Refresh token expired");
        }

        // 3) reuse detection: якщо токен вже був використаний для rotation або logout
        if (current.isRevoked() || current.getReplacedBy() != null) {
            // компрометація: відрубаємо всю family, щоб атакер не міг продовжити
            revokeFamilyInternal(family.getId(), now);
            throw new RefreshReuseDetectedException("Refresh reuse detected - family revoked");
        }

        // 4) створюємо новий refresh
        String newRaw = timeTokenUtils.generateRawToken();
        String newHash = timeTokenUtils.hash(newRaw);

        RefreshToken next = RefreshToken.builder()
                .user(current.getUser())
                .family(family)
                .tokenHash(newHash)
                .issuedAt(now)
                .expiresAt(now.plus(refreshTtlDays, ChronoUnit.DAYS))
                .build();

        next = refreshTokenRepository.save(next);

        // 5) старий refresh позначаємо як "замінили" (replaced_by)
        current.setReplacedBy(next.getId());
        current.setRevokedAt(now); // важливо: блокує повторне використання
        refreshTokenRepository.save(current);

        // 6) оновлюємо метадані family
        familyRepository.touch(family.getId(), now, normalizeAndTruncate(userAgent, 512), InetAddressUtils.parseIp(ip));

        return new RefreshIssueResult(newRaw, family.getId());
    }

    /**
     * Logout по refresh cookie: знаходимо токен -> ревокаємо family (або тільки токен).
     * Я рекомендую revoke family, бо це "logout device".
     * // logout без токена просто нічого не робить
     */
    @Transactional
    public void revokeByRefresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        Instant now = Instant.now();
        String hash = timeTokenUtils.hash(rawRefreshToken);

        refreshTokenRepository.findByTokenHash(hash)
                .ifPresent(token -> revokeFamilyInternal(token.getFamily().getId(), now));
    }

    /**
     * Logout-all: ревокаємо всі families користувача.
     */
    @Transactional
    public void revokeAllForUser(long userId) {
        Instant now = Instant.now();
        familyRepository.revokeAllForUser(userId, now);
        refreshTokenRepository.revokeAllForUser(userId, now);
    }

    /**
     * Device-level logout: revoke конкретної family.
     */
    @Transactional
    public void revokeFamily(long familyId, long userId) {
        Instant now = Instant.now();
        RefreshTokenFamily family = familyRepository.findByIdAndUser_Id(familyId, userId)
                .orElseThrow(() -> new InvalidRefreshTokenException("Family not found"));
        revokeFamilyInternal(family.getId(), now);
    }

    private void revokeFamilyInternal(long familyId, Instant now) {
        familyRepository.revokeFamily(familyId, now);
        refreshTokenRepository.revokeAllInFamily(familyId, now);
    }
}
