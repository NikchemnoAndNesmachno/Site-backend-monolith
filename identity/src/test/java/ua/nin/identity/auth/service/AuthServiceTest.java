package ua.nin.identity.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ua.nin.contract.profile.ProfileCreation;
import ua.nin.identity.auth.dto.*;
import ua.nin.identity.auth.exception.exceptions.*;
import ua.nin.identity.auth.mapper.MeResponseMapper;
import ua.nin.identity.auth.model.Credential;
import ua.nin.identity.auth.model.Role;
import ua.nin.identity.auth.model.Status;
import ua.nin.identity.auth.model.User;
import ua.nin.identity.auth.repository.CredentialRepository;
import ua.nin.identity.auth.repository.UserRepository;
import ua.nin.identity.profile.model.Privacy;
import ua.nin.identity.profile.model.Profile;
import ua.nin.identity.profile.repository.ProfileRepository;
import ua.nin.identity.profile.service.ProfileService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ua.nin.common.constant.ErrorMessage.EMAIL_ALREADY_EXISTS;
import static ua.nin.common.constant.ErrorMessage.USERNAME_ALREADY_EXISTS;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;
    @Mock
    private CredentialRepository credentialRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private ProfileCreation profileCreation;

    @Mock
    private AccessTokenService accessTokenService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private EmailVerificationService emailVerificationService;

    @Mock
    private MeResponseMapper meResponseMapper;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerUserTest_existsByEmail_exception() {
        RegisterRequest request = mockRequest();

        doReturn(true).when(userRepository).existsByEmail(request.email());

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining(EMAIL_ALREADY_EXISTS);

        verify(userRepository).existsByEmail("test@gmail.com");
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(profileRepository, accessTokenService, refreshTokenService, credentialRepository, passwordEncoder, emailVerificationService);

    }

    @Test
    void registerUserTest_existsByUsername_exception() {
        RegisterRequest request = mockRequest();

        doReturn(false).when(userRepository).existsByEmail(request.email());
        doReturn(true).when(profileRepository).existsByUsername(request.username());

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessageContaining(USERNAME_ALREADY_EXISTS);

        verify(userRepository).existsByEmail("test@gmail.com");
        verify(profileRepository).existsByUsername("test");
        verifyNoMoreInteractions(profileRepository, userRepository);
        verifyNoInteractions(accessTokenService, refreshTokenService, credentialRepository, passwordEncoder, emailVerificationService);
    }

    @Test
    void registerUserTest_success() {
        RegisterRequest request = mockRequest();

        doReturn(false).when(userRepository).existsByEmail(request.email());
        doReturn(false).when(profileRepository).existsByUsername(request.username());
        doReturn("encodedTest123").when(passwordEncoder).encode(request.password());
        doAnswer(inv -> {
            User u = inv.getArgument(0, User.class);
            u.setId(1L);
            return u;
        }).when(userRepository).save(any(User.class));

        authService.register(request);

        verify(userRepository).existsByEmail("test@gmail.com");
        verify(profileRepository).existsByUsername("test");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("test@gmail.com", userCaptor.getValue().getEmail());
        assertEquals(Status.PENDING_EMAIL, userCaptor.getValue().getStatus());
        assertEquals(Role.USER, userCaptor.getValue().getRole());

        verify(passwordEncoder).encode("test123");

        ArgumentCaptor<Credential> credentialCaptor = ArgumentCaptor.forClass(Credential.class);
        verify(credentialRepository).save(credentialCaptor.capture());
        assertSame(userCaptor.getValue(), credentialCaptor.getValue().getUser());
        assertEquals("encodedTest123", credentialCaptor.getValue().getPasswordHash());
        assertEquals(0, credentialCaptor.getValue().getFailedLoginAttempts());

        ArgumentCaptor<User> userForEmailCaptor = ArgumentCaptor.forClass(User.class);
        verify(emailVerificationService).send(userForEmailCaptor.capture());
        assertSame(userCaptor.getValue(), userForEmailCaptor.getValue());

        verifyNoMoreInteractions(userRepository, profileRepository, credentialRepository, passwordEncoder, emailVerificationService);
        verifyNoInteractions(accessTokenService, refreshTokenService);
    }

    @Test
    void loginUserTest_bannedUser_forbidden() {
        LoginRequest request = new LoginRequest("user@site.com", "password");
        User user = User.builder()
                .id(12L)
                .email("user@site.com")
                .status(Status.BANNED)
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail("user@site.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(request, "ua", "127.0.0.1"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("User is not allowed");

        verify(userRepository).findByEmail("user@site.com");
        verifyNoInteractions(credentialRepository, passwordEncoder, refreshTokenService, accessTokenService);
    }

    @Test
    void loginUserTest_invalidPassword_badCredentials() {
        LoginRequest request = new LoginRequest("user@site.com", "bad-pass");
        User user = User.builder()
                .id(12L)
                .email("user@site.com")
                .status(Status.ACTIVE)
                .role(Role.USER)
                .build();
        Credential cred = Credential.builder()
                .user(user)
                .userId(user.getId())
                .passwordHash("hash")
                .failedLoginAttempts(0)
                .build();

        when(userRepository.findByEmail("user@site.com")).thenReturn(Optional.of(user));
        when(credentialRepository.findById(user.getId())).thenReturn(Optional.of(cred));
        when(passwordEncoder.matches("bad-pass", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request, "ua", "127.0.0.1"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid credentials");

        verify(userRepository).findByEmail("user@site.com");
        verify(credentialRepository).findById(user.getId());
        verify(passwordEncoder).matches("bad-pass", "hash");
        verifyNoInteractions(refreshTokenService, accessTokenService);
    }

    @Test
    void loginUserTest_success() {
        LoginRequest request = new LoginRequest("user@site.com", "secret");
        User user = User.builder()
                .id(12L)
                .email("user@site.com")
                .status(Status.ACTIVE)
                .role(Role.USER)
                .build();
        Credential cred = Credential.builder()
                .user(user)
                .userId(user.getId())
                .passwordHash("hash")
                .failedLoginAttempts(0)
                .build();

        when(userRepository.findByEmail("user@site.com")).thenReturn(Optional.of(user));
        when(credentialRepository.findById(user.getId())).thenReturn(Optional.of(cred));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);
        when(refreshTokenService.issueNew(user, "ua", "127.0.0.1"))
                .thenReturn(new IssueNewResult("refresh-token", 44L));
        when(accessTokenService.createAccessToken(12L, List.of("USER"))).thenReturn("access-token");

        AuthResult result = authService.login(request, "ua", "127.0.0.1");

        assertEquals("access-token", result.authResponse().accessToken());
        assertEquals("refresh-token", result.refreshToken());
        assertEquals(12L, result.authResponse().userId());
        assertEquals("USER", result.authResponse().role());

        verify(userRepository).save(user);
    }

    @Test
    void refreshUserTest_success() {
        User user = User.builder()
                .id(5L)
                .email("user@site.com")
                .status(Status.ACTIVE)
                .role(Role.USER)
                .build();

        when(refreshTokenService.rotate("refresh-token", "ua", "127.0.0.1"))
                .thenReturn(new RefreshIssueResult("new-refresh", 50L));
        when(refreshTokenService.getUserByFamily(50L)).thenReturn(user);
        when(accessTokenService.createAccessToken(5L, List.of("USER"))).thenReturn("access");

        AuthResult result = authService.refresh("refresh-token", "ua", "127.0.0.1");

        assertEquals("access", result.authResponse().accessToken());
        assertEquals("new-refresh", result.refreshToken());
    }

    @Test
    void logoutTest_callsRefreshTokenService() {
        authService.logout("refresh-token");

        verify(refreshTokenService).revokeByRefresh("refresh-token");
    }

    @Test
    void logoutAllTest() {
        long userId = 42L;
        authService.logoutAll(userId);

        verify(refreshTokenService).revokeAllForUser(42L);
    }

    @Test
    void meTest_userNotFound_throws() {
        long userId = 19L;

        when(userRepository.findById(19L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.me(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void meTest_success() {
        long userId = 19L;

        User user = User.builder().id(userId).email("me@site.com").status(Status.ACTIVE).role(Role.USER).build();
        MeResponse response = new MeResponse(19L, "me@site.com", Status.ACTIVE.name(), Role.USER.name());

        when(userRepository.findById(19L)).thenReturn(Optional.of(user));
        when(meResponseMapper.toDto(user)).thenReturn(response);

        MeResponse result = authService.me(userId);

        assertEquals(19L, result.userId());
        assertEquals("me@site.com", result.email());
    }

    private static RegisterRequest mockRequest() {
        return RegisterRequest
                .builder()
                .email("test@gmail.com")
                .username("test")
                .password("test123")
                .build();
    }
}