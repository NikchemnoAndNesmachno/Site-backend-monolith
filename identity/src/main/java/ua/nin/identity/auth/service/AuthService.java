package ua.nin.identity.auth.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.nin.identity.auth.dto.*;
import ua.nin.identity.auth.exception.exceptions.BadCredentialsException;
import ua.nin.identity.auth.exception.exceptions.ConflictException;
import ua.nin.identity.auth.exception.exceptions.ForbiddenException;
import ua.nin.identity.auth.exception.exceptions.NotFoundException;
import ua.nin.identity.auth.mapper.MeResponseMapper;
import ua.nin.identity.auth.model.Credential;
import ua.nin.identity.auth.model.Role;
import ua.nin.identity.auth.model.Status;
import ua.nin.identity.auth.model.User;
import ua.nin.identity.auth.repository.CredentialRepository;
import ua.nin.identity.auth.repository.UserRepository;
import ua.nin.common.util.StringHelperUtils;
import ua.nin.identity.auth.util.SecurityUtils;
import ua.nin.identity.profile.model.Privacy;
import ua.nin.identity.profile.model.Profile;
import ua.nin.identity.profile.repository.ProfileRepository;

import static ua.nin.common.util.StringHelperUtils.normalizeEmail;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;
    private final ProfileRepository profileRepository;

    private final PasswordEncoder passwordEncoder;

    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationService emailVerificationService;

    private final MeResponseMapper meResponseMapper;

    @Value("${jwt.access-ttl-minutes:10}")
    private long accessTtlMinutes;

    // ---------------- REGISTER ----------------
    @Transactional
    public void register(@Valid RegisterRequest req) {
        String email = StringHelperUtils.normalizeEmail(req.email());
        String username = req.username().trim();

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already exists");
        }
        if (profileRepository.existsByUsername(username)) {
            throw new ConflictException("Username already exists");
        }

        User user = User.builder()
                .email(email)
                .status(Status.PENDING_EMAIL)
                .role(Role.USER)
                .build();

        user = userRepository.save(user);

        Credential cred = Credential.builder()
                .user(user)
                .passwordHash(passwordEncoder.encode(req.password()))
                .passwordUpdatedAt(Instant.now())
                .failedLoginAttempts(0)
                .build();
        credentialRepository.save(cred);

        Profile profile = Profile.builder()
                .user(user)
                .username(username)
                .displayName(username)
                .privacy(Privacy.PUBLIC)
                .build();
        profileRepository.save(profile);

        // Тут потім підключиш email verification (хвиля 2)
        emailVerificationService.send(user);
    }

    // ---------------- LOGIN ----------------
    @Transactional
    public AuthResult login(@Valid LoginRequest req, String userAgent, String ip) {
        String email = normalizeEmail(req.email());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (user.getStatus() == Status.BANNED || user.getStatus() == Status.DELETED) {
            throw new ForbiddenException("User is not allowed to login");
        }

        Credential cred = credentialRepository.findById(user.getId())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(req.password(), cred.getPasswordHash())) {
            cred.incrementFailedLoginAttempts();
            throw new BadCredentialsException("Invalid credentials");
        }

        // optional: last_login_at
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        // refresh issue (family + token)
        IssueNewResult refresh = refreshTokenService.issueNew(user, userAgent, ip);

        // access token
        String role = user.getRole().name();
        String access = accessTokenService.createAccessToken(user.getId(), List.of(role));

        AuthResponse response = new AuthResponse(
                access,
                "Bearer",
                accessTtlMinutes * 60,
                user.getId(),
                role
        );

        return new AuthResult(response, refresh.rawRefreshToken());
    }

    // ---------------- REFRESH ----------------
    @Transactional
    public AuthResult refresh(String rawRefreshToken, String userAgent, String ip) {
        RefreshIssueResult rotated = refreshTokenService.rotate(rawRefreshToken, userAgent, ip);

        // userId краще діставати з БД через знайдений refresh-token (надійніше)
        // але якщо rotate повертає familyId тільки, то userId треба витягнути з БД.
        // Рекомендується: rotate повертає також userId.
        //
        // Тому: змінити RefreshIssueResult -> (rawRefreshToken, familyId, userId, role)
        //
        // Але якщо поки не хочеться міняти record — робимо lookup через family:
        User user = refreshTokenService.getUserByFamily(rotated.familyId()); // додамо метод нижче

        String role = user.getRole().name();
        String access = accessTokenService.createAccessToken(user.getId(), List.of(role));

        AuthResponse response = new AuthResponse(
                access,
                "Bearer",
                accessTtlMinutes * 60,
                user.getId(),
                role
        );

        return new AuthResult(response, rotated.rawRefreshToken());
    }

    // ---------------- LOGOUT ----------------
    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenService.revokeByRefresh(rawRefreshToken);
    }

    // ---------------- LOGOUT ALL ----------------
    @Transactional
    public void logoutAll() {
        long userId = SecurityUtils.currentUserId();
        refreshTokenService.revokeAllForUser(userId);
    }

    // ---------------- ME ----------------
    @Transactional(readOnly = true)
    public MeResponse me() {
        long userId = SecurityUtils.currentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return meResponseMapper.toDto(user);
    }
}
