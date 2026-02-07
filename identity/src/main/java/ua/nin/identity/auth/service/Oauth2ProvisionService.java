package ua.nin.identity.auth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ua.nin.identity.auth.dto.AuthResponse;
import ua.nin.identity.auth.dto.AuthResult;
import ua.nin.identity.auth.dto.IssueNewResult;
import ua.nin.identity.auth.dto.Oauth2UserDto;
import ua.nin.identity.auth.exception.exceptions.UserNotFoundException;
import ua.nin.identity.auth.model.OAuth2Identity;
import ua.nin.identity.auth.model.Role;
import ua.nin.identity.auth.model.Status;
import ua.nin.identity.auth.model.User;
import ua.nin.identity.auth.repository.CredentialRepository;
import ua.nin.identity.auth.repository.OAuth2IdentityRepository;
import ua.nin.identity.auth.repository.UserRepository;
import ua.nin.identity.profile.model.Privacy;
import ua.nin.identity.profile.model.Profile;
import ua.nin.identity.profile.repository.ProfileRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class Oauth2ProvisionService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final OAuth2IdentityRepository oAuth2IdentityRepository;

    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;

    @Value("${jwt.access-ttl-minutes:10}")
    private long accessTtlMinutes;

    /*
    public class GoogleUserDto {
        private String provider
        private String googleProviderId;
        private String email;
        private Boolean emailVerified;
        private String name;
        private String picture;
    }
     */


    @Transactional
    public AuthResult provisionUser(Oauth2UserDto dto, String userAgent, String ip) {
        Optional<OAuth2Identity> linkedUserIdentity = oAuth2IdentityRepository.findByProviderAndSubject(dto.provider(), dto.providerId());

        if (linkedUserIdentity.isPresent()) {
            OAuth2Identity identity = linkedUserIdentity.get();
            updateUserProfile(identity, dto);
            User user = userRepository.findByEmail(dto.email()).get();
            return issueTokens(user, userAgent, ip);
        }

        Optional<User> userByEmail = userRepository.findByEmail(dto.email());

        if (userByEmail.isPresent()) {
            User user = userByEmail.get();
            linkOauth2Provider(user, dto);
            return issueTokens(user, userAgent, ip);
        }

        User newUser = createNewOauth2Identity(dto);
        return issueTokens(newUser, userAgent, ip);
    }

    private void updateUserProfile(OAuth2Identity identity, Oauth2UserDto dto) {
        identity.setEmailVerified(dto.emailVerified());
        oAuth2IdentityRepository.save(identity);
//        if (dto.getPicture() != null && !dto.getPicture().isEmpty()) {
//            user.setProfilePicturePath(dto.getPicture());
//        }
    }

    private void linkOauth2Provider(User user, Oauth2UserDto dto) {
        OAuth2Identity oAuth2Identity = OAuth2Identity.builder()
                .userId(user.getId())
                .provider(dto.provider())
                .subject(dto.providerId())
                .email(dto.email())
                .emailVerified(dto.emailVerified())
                .build();
        oAuth2IdentityRepository.save(oAuth2Identity);

        if (user.getStatus().equals(Status.PENDING_EMAIL) && dto.emailVerified()) {
            user.setStatus(Status.ACTIVE);
            userRepository.save(user);
        }

//        if (user.getProfilePicturePath() == null || user.getProfilePicturePath().isEmpty()) {
//            user.setProfilePicturePath(dto.getPicture());
//        }
    }

    private User createNewOauth2Identity(Oauth2UserDto dto) {
        User user = User.builder()
                .email(dto.email())
                .status(dto.emailVerified()
                        ? Status.ACTIVE
                        : Status.PENDING_EMAIL)
                .role(Role.USER)
                .build();
        user = userRepository.save(user);

        OAuth2Identity oAuth2Identity = OAuth2Identity.builder()
                .userId(user.getId())
                .provider(dto.provider())
                .subject(dto.providerId())
                .email(dto.email())
                .emailVerified(dto.emailVerified())
                .build();
        oAuth2IdentityRepository.save(oAuth2Identity);

        Profile profile = Profile.builder()
                .user(user)
                .username(dto.name())
                .displayName(dto.name())
                .privacy(Privacy.PUBLIC)
                .build();
        profileRepository.save(profile);

        return user;
    }

    private AuthResult issueTokens(User user, String userAgent, String ip) {
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
}