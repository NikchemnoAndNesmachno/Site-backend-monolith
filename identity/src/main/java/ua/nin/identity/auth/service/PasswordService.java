package ua.nin.identity.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.nin.identity.auth.exception.exceptions.BadCredentialsException;
import ua.nin.identity.auth.exception.exceptions.InvalidTokenException;
import ua.nin.identity.auth.model.Credential;
import ua.nin.identity.auth.model.PasswordResetToken;
import ua.nin.identity.auth.model.Status;
import ua.nin.identity.auth.model.User;
import ua.nin.identity.auth.repository.CredentialRepository;
import ua.nin.identity.auth.repository.PasswordResetTokenRepository;
import ua.nin.identity.auth.repository.UserRepository;
import ua.nin.identity.auth.util.StringHelperUtils;
import ua.nin.identity.auth.util.SecurityUtils;
import ua.nin.identity.auth.util.TimeTokenUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private final PasswordEncoder passwordEncoder;
    private final TimeTokenUtils timeTokenUtils;

    private final RefreshTokenService refreshTokenService;

    @Value("${security.password-reset.ttl-minutes:30}")
    private long resetTtlMinutes;

    /**
     * Забули пароль:
     * - завжди 204 у контролері, незалежно від існування email
     * - якщо user існує і активний -> створюємо reset token (hash у БД), raw віддаємо "на пошту"
     */
    @Transactional
    public void forgot(String email) {
        String normalized = StringHelperUtils.normalizeEmail(email);
        if (normalized == null) return;

        Optional<User> userOpt = userRepository.findByEmail(normalized);
        if (userOpt.isEmpty()) {
            return; // не палимо, що email не існує
        }

        User user = userOpt.get();
        if (user.getStatus() == Status.BANNED || user.getStatus() == Status.DELETED) {
            return; // також не палимо деталі
        }

        String raw = timeTokenUtils.generateRawToken();
        String hash = timeTokenUtils.hash(raw);

        Instant now = Instant.now();
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .tokenHash(hash)
                .expiresAt(now.plus(resetTtlMinutes, ChronoUnit.MINUTES))
                .usedAt(null)
                .build();

        passwordResetTokenRepository.save(token);

        // TODO: інтегрувати EmailService
        // В dev можна тимчасово логати:
        System.out.println("[DEV] Password reset token for " + normalized + ": " + raw);
    }

    /**
     * Reset password по одноразовому токену.
     * - token -> hash -> знаходимо запис
     * - перевіряємо expires/used
     * - міняємо пароль, помічаємо токен used_at
     */
    @Transactional
    public void reset(String rawToken, String newPassword) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidTokenException("Missing reset token");
        }

        String hash = timeTokenUtils.hash(rawToken);
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new InvalidTokenException("Invalid reset token"));

        Instant now = Instant.now();
        if (token.isUsed()) {
            throw new InvalidTokenException("Reset token already used");
        }
        if (token.isExpired(now)) {
            throw new InvalidTokenException("Reset token expired");
        }

        User user = token.getUser();

        Credential cred = credentialRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("Credential missing"));

        cred.setPasswordHash(passwordEncoder.encode(newPassword));
        cred.setPasswordUpdatedAt(now);
        cred.setFailedLoginAttempts(0);
        cred.setLockUntil(null);
        credentialRepository.save(cred);

        token.setUsedAt(now);
        passwordResetTokenRepository.save(token);

        // Опціонально: logout-all після reset (щоб викинути всі refresh)
        refreshTokenService.revokeAllForUser(user.getId());
    }

    /**
     * Change password для залогіненого (access token required).
     */
    @Transactional
    public void change(String currentPassword, String newPassword) {
        long userId = SecurityUtils.currentUserId();

        Credential cred = credentialRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Credential missing"));

        if (!passwordEncoder.matches(currentPassword, cred.getPasswordHash())) {
            throw new BadCredentialsException("Current password invalid");
        }

        Instant now = Instant.now();
        cred.setPasswordHash(passwordEncoder.encode(newPassword));
        cred.setPasswordUpdatedAt(now);
        credentialRepository.save(cred);

        // Опціонально: logout-all після change (security best practice)
        refreshTokenService.revokeAllForUser(userId);
    }
}
