package ua.nin.identity.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.nin.identity.auth.exception.exceptions.InvalidTokenException;
import ua.nin.identity.auth.model.EmailVerificationToken;
import ua.nin.identity.auth.model.Status;
import ua.nin.identity.auth.model.User;
import ua.nin.identity.auth.repository.EmailVerificationTokenRepository;
import ua.nin.identity.auth.repository.UserRepository;
import ua.nin.common.util.StringHelperUtils;
import ua.nin.identity.auth.util.TimeTokenUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final TimeTokenUtils timeTokenUtils;

    @Value("${security.email-verify.ttl-minutes:30}")
    private long verifyTtlMinutes;

    private final EmailSenderService emailSenderService;

    /**
     * VERIFY:
     * - token -> hash -> знаходимо запис
     * - перевіряємо expires/used
     * - users.status = ACTIVE (якщо був PENDING_EMAIL)
     * - token.used_at = now
     */
    @Transactional
    public void verify(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidTokenException("Missing verification token");
        }

        String hash = timeTokenUtils.hash(rawToken);

        EmailVerificationToken token = tokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));

        Instant now = Instant.now();

        if (token.isUsed()) {
            throw new InvalidTokenException("Verification token already used");
        }
        if (token.isExpired(now)) {
            throw new InvalidTokenException("Verification token expired");
        }

        User user = token.getUser();

        // якщо вже ACTIVE — можна просто позначити токен used і закінчити
        if (user.getStatus() == Status.PENDING_EMAIL) {
            user.setStatus(Status.ACTIVE);
            userRepository.save(user);
        }

        token.setUsedAt(now);
        tokenRepository.save(token);
    }

    @Transactional
    public void send(User user) {
        String email = user.getEmail();

        String normalizedEmail = StringHelperUtils.normalizeEmail(email);
        if (normalizedEmail == null) return;

        String raw = timeTokenUtils.generateRawToken();
        String hash = timeTokenUtils.hash(raw);

        Instant now = Instant.now();

        EmailVerificationToken token = EmailVerificationToken.builder()
                .user(user)
                .tokenHash(hash)
                .expiresAt(now.plus(verifyTtlMinutes, ChronoUnit.MINUTES))
                .usedAt(null)
                .build();

        tokenRepository.save(token);

        emailSenderService.sendEmailVerification(normalizedEmail, raw);
    }

    /**
     * RESEND:
     * - завжди "успіх" (контролер повертає 204)
     * - якщо email існує і status=PENDING_EMAIL -> створюємо новий токен
     */
    @Transactional
    public void resend(String email) {
        String normalizedEmail = StringHelperUtils.normalizeEmail(email);
        if (normalizedEmail == null) return;

        Optional<User> userOpt = userRepository.findByEmail(normalizedEmail);
        if (userOpt.isEmpty()) {
            return; // не палимо існування email
        }

        User user = userOpt.get();

        // якщо вже ACTIVE/BANNED/DELETED — нічого не робимо (також не палимо)
        if (user.getStatus() != Status.PENDING_EMAIL) {
            return;
        }

        String raw = timeTokenUtils.generateRawToken();
        String hash = timeTokenUtils.hash(raw);

        Instant now = Instant.now();

        EmailVerificationToken token = EmailVerificationToken.builder()
                .user(user)
                .tokenHash(hash)
                .expiresAt(now.plus(verifyTtlMinutes, ChronoUnit.MINUTES))
                .usedAt(null)
                .build();

        tokenRepository.save(token);

        emailSenderService.sendEmailVerification(normalizedEmail, raw);
    }
}
