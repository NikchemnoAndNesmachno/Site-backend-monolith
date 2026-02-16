package ua.nin.identity.auth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.nin.identity.auth.dto.OAuth2UserDto;
import ua.nin.identity.auth.exception.exceptions.NotFoundException;
import ua.nin.identity.auth.model.OAuth2Identity;
import ua.nin.identity.auth.model.Role;
import ua.nin.identity.auth.model.Status;
import ua.nin.identity.auth.model.User;
import ua.nin.identity.auth.repository.OAuth2IdentityRepository;
import ua.nin.identity.auth.repository.UserRepository;
import ua.nin.identity.profile.model.Privacy;
import ua.nin.identity.profile.model.Profile;
import ua.nin.identity.profile.repository.ProfileRepository;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class Oauth2ProvisionService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final OAuth2IdentityRepository identityRepository;

    @Transactional
    public User provision(OAuth2UserDto dto) {
        Instant now = Instant.now();

        // 1) by provider+subject
        var existingIdentity = identityRepository.findByProviderAndSubject(dto.provider(), dto.providerId());
        if (existingIdentity.isPresent()) {
            Long userId = existingIdentity.get().getUserId();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found: " + userId));
            touchIdentity(existingIdentity.get(), dto, now);
            return user;
        }

        // 2) by email (only if verified and non-null)
        User user = null;
        if (dto.email() != null && dto.emailVerified()) {
            user = userRepository.findByEmail(dto.email()).orElse(null);
        }

        // 3) create if not exists
        if (user == null) {
            user = createUser(dto, now);
            user = userRepository.save(user);

            // create profile if missing
            createProfileIfMissing(user, dto, now);
        } else {
            // if was PENDING_EMAIL, but now is verified through OAuth — do ACTIVE
            if (Status.PENDING_EMAIL.equals(user.getStatus())) {
                user.setStatus(Status.ACTIVE);
                user.setUpdatedAt(now);
                userRepository.save(user);
            }
            createProfileIfMissing(user, dto, now);
        }

        // 4) link identity
        OAuth2Identity identity = OAuth2Identity.builder()
                .userId(user.getId())
                .provider(dto.provider())
                .subject(dto.providerId())
                .email(dto.email())
                .emailVerified(dto.emailVerified())
                .createdAt(now)
                .updatedAt(now)
                .build();

        identityRepository.save(identity);
        return user;
    }

    private void touchIdentity(OAuth2Identity identity, OAuth2UserDto dto, Instant now) {
        identity.setEmail(dto.email());
        identity.setEmailVerified(dto.emailVerified());
        identity.setUpdatedAt(now);
        identityRepository.save(identity);
    }

    private User createUser(OAuth2UserDto dto, Instant now) {
        User u = new User();
        u.setEmail(dto.email() != null ? dto.email() : ("no-email@" + dto.provider().name().toLowerCase() + ".local"));
        u.setRole(Role.USER);
        u.setStatus(dto.emailVerified() ? Status.ACTIVE : Status.PENDING_EMAIL);
        u.setCreatedAt(now);
        u.setUpdatedAt(now);
        u.setLastLoginAt(now);
        return u;
    }

    private void createProfileIfMissing(User user, OAuth2UserDto dto, Instant now) {
        if (profileRepository.findByUserId(user.getId()).isPresent()) return;

        String base = deriveUsername(dto);
        String username = uniqueUsername(base);

        Profile p = new Profile();
        p.setUserId(user.getId());
        p.setUsername(username);
        p.setDisplayName(dto.name());
        p.setBio(null);
        p.setPrivacy(Privacy.PUBLIC);
        p.setLocale(null);
        p.setTimezone(null);
        p.setCreatedAt(now);
        p.setUpdatedAt(now);

        profileRepository.saveAndFlush(p);
    }

    private String deriveUsername(OAuth2UserDto info) {
        if (info.email() != null && info.email().contains("@")) {
            return info.email().substring(0, info.email().indexOf('@')).replaceAll("[^a-zA-Z0-9_.]", "_");
        }
        if (info.name() != null && !info.name().isBlank()) {
            return info.name().trim().replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9_.]", "_");
        }
        return info.provider().name().toLowerCase() + "_user";
    }

    private String uniqueUsername(String base) {
        String b = base.length() > 32
                ? base.substring(0, 32)
                : base;
        if (!profileRepository.existsByUsername(b)) return b;

        String candidate = b + "_" + UUID.randomUUID();
        candidate = candidate.length() > 64 ? candidate.substring(0, 64) : candidate;
        if (!profileRepository.existsByUsername(candidate)) return candidate;

        throw new IllegalStateException("Failed to allocate unique username.");
    }
}