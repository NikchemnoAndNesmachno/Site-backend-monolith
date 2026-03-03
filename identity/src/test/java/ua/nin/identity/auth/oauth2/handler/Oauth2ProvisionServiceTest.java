package ua.nin.identity.auth.oauth2.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.nin.identity.auth.dto.OAuth2UserDto;
import ua.nin.identity.auth.exception.exceptions.FailedUniqueUsernameCreationException;
import ua.nin.identity.auth.exception.exceptions.UserNotFoundException;
import ua.nin.identity.auth.model.OAuth2Identity;
import ua.nin.identity.auth.model.Provider;
import ua.nin.identity.auth.model.Role;
import ua.nin.identity.auth.model.Status;
import ua.nin.identity.auth.model.User;
import ua.nin.identity.auth.repository.OAuth2IdentityRepository;
import ua.nin.identity.auth.repository.UserRepository;
import ua.nin.identity.profile.model.Profile;
import ua.nin.identity.profile.repository.ProfileRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Oauth2ProvisionServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private OAuth2IdentityRepository identityRepository;

    @InjectMocks private Oauth2ProvisionService service;

    @Captor private ArgumentCaptor<User> userCaptor;
    @Captor private ArgumentCaptor<OAuth2Identity> identityCaptor;
    @Captor private ArgumentCaptor<Profile> profileCaptor;

    private static OAuth2UserDto dtoGoogleVerified(String sub, String email, String name) {
        return OAuth2UserDto.builder()
                .provider(Provider.GOOGLE)
                .providerId(sub)
                .email(email)
                .emailVerified(true)
                .name(name)
                .picture("pic")
                .build();
    }

    private static OAuth2UserDto dtoGoogleUnverifiedNoEmail(String sub) {
        return OAuth2UserDto.builder()
                .provider(Provider.GOOGLE)
                .providerId(sub)
                .email(null)
                .emailVerified(false)
                .name("NoEmail User")
                .picture(null)
                .build();
    }

    @Test
    void provision_whenIdentityExists_shouldTouchIdentity_andReturnUser() {
        OAuth2UserDto dto = dtoGoogleVerified("sub-1", "a@b.com", "Alice");

        OAuth2Identity identity = OAuth2Identity.builder()
                .userId(10L)
                .provider(Provider.GOOGLE)
                .subject("sub-1")
                .email("old@b.com")
                .emailVerified(false)
                .build();

        when(identityRepository.findByProviderAndSubject(dto.provider(), dto.providerId()))
                .thenReturn(Optional.of(identity));

        User user = new User();
        user.setId(10L);
        user.setEmail("a@b.com");
        user.setRole(Role.USER);
        user.setStatus(Status.ACTIVE);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        User res = service.provision(dto);

        assertThat(res).isSameAs(user);

        // touchIdentity повинен оновити поля і зберегти identity
        verify(identityRepository).save(identityCaptor.capture());
        OAuth2Identity saved = identityCaptor.getValue();
        assertThat(saved.getEmail()).isEqualTo("a@b.com");
        assertThat(saved.isEmailVerified()).isTrue();
        assertThat(saved.getUpdatedAt()).isNotNull();

        verify(userRepository, never()).save(any(User.class));
        verify(profileRepository, never()).saveAndFlush(any(Profile.class));
    }

    @Test
    void provision_whenIdentityExistsButUserMissing_shouldThrowNotFound() {
        OAuth2UserDto dto = dtoGoogleVerified("sub-1", "a@b.com", "Alice");

        OAuth2Identity identity = OAuth2Identity.builder()
                .userId(10L)
                .provider(Provider.GOOGLE)
                .subject("sub-1")
                .build();

        when(identityRepository.findByProviderAndSubject(dto.provider(), dto.providerId()))
                .thenReturn(Optional.of(identity));

        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.provision(dto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found: 10");

        verify(identityRepository, never()).save(any(OAuth2Identity.class));
    }

    @Test
    void provision_whenUserFoundByVerifiedEmail_andPendingEmail_shouldActivate_createProfile_linkIdentity() {
        OAuth2UserDto dto = dtoGoogleVerified("sub-2", "pending@b.com", "Pending");

        when(identityRepository.findByProviderAndSubject(dto.provider(), dto.providerId()))
                .thenReturn(Optional.empty());

        User existing = new User();
        existing.setId(5L);
        existing.setEmail("pending@b.com");
        existing.setRole(Role.USER);
        existing.setStatus(Status.PENDING_EMAIL);

        when(userRepository.findByEmail("pending@b.com")).thenReturn(Optional.of(existing));

        when(profileRepository.findByUserId(5L)).thenReturn(Optional.empty());
        // deriveUsername -> "pending"
        when(profileRepository.existsByUsername("pending")).thenReturn(false);

        User res = service.provision(dto);

        assertThat(res.getId()).isEqualTo(5L);
        assertThat(res.getStatus()).isEqualTo(Status.ACTIVE);

        // user повинен бути збережений (через зміну статуса)
        verify(userRepository, atLeastOnce()).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getStatus()).isEqualTo(Status.ACTIVE);

        // profile створюється
        verify(profileRepository).saveAndFlush(profileCaptor.capture());
        Profile p = profileCaptor.getValue();
        assertThat(p.getUserId()).isEqualTo(5L);
        assertThat(p.getUsername()).isEqualTo("pending");
        assertThat(p.getDisplayName()).isEqualTo("Pending");

        // identity створюється
        verify(identityRepository).save(identityCaptor.capture());
        OAuth2Identity identity = identityCaptor.getValue();
        assertThat(identity.getUserId()).isEqualTo(5L);
        assertThat(identity.getProvider()).isEqualTo(Provider.GOOGLE);
        assertThat(identity.getSubject()).isEqualTo("sub-2");
        assertThat(identity.getEmail()).isEqualTo("pending@b.com");
        assertThat(identity.isEmailVerified()).isTrue();
    }

    @Test
    void provision_whenNoUser_shouldCreateUser_withFallbackEmail_pendingAndCreateProfile_andLinkIdentity() {
        OAuth2UserDto dto = dtoGoogleUnverifiedNoEmail("sub-3");

        when(identityRepository.findByProviderAndSubject(dto.provider(), dto.providerId()))
                .thenReturn(Optional.empty());

        // email is null -> findByEmail не повинен вызиваться взагалі (або може, але в тебе умова захищає)
        // userRepository.save повинен присвоїти id
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            if (u.getId() == null) u.setId(100L);
            return u;
        });

        when(profileRepository.findByUserId(100L)).thenReturn(Optional.empty());
        // deriveUsername: при email null він бере name => "NoEmail_User"
        when(profileRepository.existsByUsername("NoEmail_User")).thenReturn(false);

        User res = service.provision(dto);

        assertThat(res.getId()).isEqualTo(100L);
        assertThat(res.getRole()).isEqualTo(Role.USER);
        assertThat(res.getStatus()).isEqualTo(Status.PENDING_EMAIL);
        assertThat(res.getEmail()).isEqualTo("no-email@google.local");

        verify(profileRepository).saveAndFlush(profileCaptor.capture());
        assertThat(profileCaptor.getValue().getUsername()).isEqualTo("NoEmail_User");

        verify(identityRepository).save(identityCaptor.capture());
        assertThat(identityCaptor.getValue().getUserId()).isEqualTo(100L);
        assertThat(identityCaptor.getValue().getEmail()).isNull();
        assertThat(identityCaptor.getValue().isEmailVerified()).isFalse();
    }

    @Test
    void provision_uniqueUsername_whenBaseTaken_shouldAppendUuid_andKeepLengthLimit() {
        OAuth2UserDto dto = dtoGoogleVerified("sub-4", "john@b.com", "John");

        when(identityRepository.findByProviderAndSubject(dto.provider(), dto.providerId()))
                .thenReturn(Optional.empty());

        User existing = new User();
        existing.setId(7L);
        existing.setEmail("john@b.com");
        existing.setRole(Role.USER);
        existing.setStatus(Status.ACTIVE);
        when(userRepository.findByEmail("john@b.com")).thenReturn(Optional.of(existing));

        when(profileRepository.findByUserId(7L)).thenReturn(Optional.empty());

        // base = "john"
        when(profileRepository.existsByUsername("john")).thenReturn(true);
        // для будь-якого іншого username (uuid варіант) -> false
        when(profileRepository.existsByUsername(argThat(s -> s != null && !s.equals("john"))))
                .thenReturn(false);

        service.provision(dto);

        verify(profileRepository).saveAndFlush(profileCaptor.capture());
        String username = profileCaptor.getValue().getUsername();

        assertThat(username).startsWith("john_");
        assertThat(username.length()).isLessThanOrEqualTo(64);
    }

    @Test
    void provision_uniqueUsername_whenBaseAndCandidateTaken_shouldThrow() {
        OAuth2UserDto dto = dtoGoogleVerified("sub-5", "kate@b.com", "Kate");

        when(identityRepository.findByProviderAndSubject(dto.provider(), dto.providerId()))
                .thenReturn(Optional.empty());

        User existing = new User();
        existing.setId(9L);
        existing.setEmail("kate@b.com");
        existing.setRole(Role.USER);
        existing.setStatus(Status.ACTIVE);
        when(userRepository.findByEmail("kate@b.com")).thenReturn(Optional.of(existing));

        when(profileRepository.findByUserId(9L)).thenReturn(Optional.empty());

        // base і candidate "зайняті"
        when(profileRepository.existsByUsername(anyString())).thenReturn(true);

        assertThatThrownBy(() -> service.provision(dto))
                .isInstanceOf(FailedUniqueUsernameCreationException.class)
                .hasMessageContaining("Failed to allocate unique username");

        verify(profileRepository, never()).saveAndFlush(any(Profile.class));
    }

    @Test
    void provision_whenProfileAlreadyExists_shouldNotCreateProfile() {
        OAuth2UserDto dto = dtoGoogleVerified("sub-6", "hasprofile@b.com", "HP");

        when(identityRepository.findByProviderAndSubject(dto.provider(), dto.providerId()))
                .thenReturn(Optional.empty());

        User existing = new User();
        existing.setId(11L);
        existing.setEmail("hasprofile@b.com");
        existing.setRole(Role.USER);
        existing.setStatus(Status.ACTIVE);
        when(userRepository.findByEmail("hasprofile@b.com")).thenReturn(Optional.of(existing));

        when(profileRepository.findByUserId(11L)).thenReturn(Optional.of(new Profile()));

        service.provision(dto);

        verify(profileRepository, never()).existsByUsername(anyString());
        verify(profileRepository, never()).saveAndFlush(any(Profile.class));

        // identity все рівно створюється
        verify(identityRepository).save(any(OAuth2Identity.class));
    }
}
