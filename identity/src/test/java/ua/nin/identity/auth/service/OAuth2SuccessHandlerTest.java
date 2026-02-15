package ua.nin.identity.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import ua.nin.identity.auth.dto.IssueNewResult;
import ua.nin.identity.auth.dto.OAuth2UserDto;
import ua.nin.identity.auth.model.Role;
import ua.nin.identity.auth.model.User;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2SuccessHandlerTest {

    @Mock private Oauth2ProvisionService provisionService;
    @Mock private AccessTokenService accessTokenService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private HttpCookieService cookieService;

    private ObjectMapper objectMapper;
    private OAuth2SuccessHandler handler;

    @Captor private ArgumentCaptor<OAuth2UserDto> oauthDtoCaptor;
    @Captor private ArgumentCaptor<List<String>> rolesCaptor;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        handler = new OAuth2SuccessHandler(
                provisionService,
                accessTokenService,
                refreshTokenService,
                cookieService,
                objectMapper
        );
        // @Value поле руками для unit-test
        ReflectionTestUtils.setField(handler, "accessTtlMinutes", 10L);
    }

    @Test
    void onAuthenticationSuccess_shouldProvisionIssueTokensSetCookie_andWriteJsonResponse() throws Exception {
        // request/response
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.USER_AGENT, "JUnit-UA");
        request.setRemoteAddr("203.0.113.10");

        MockHttpServletResponse response = new MockHttpServletResponse();

        // auth token + oidc user
        OAuth2AuthenticationToken authentication = mock(OAuth2AuthenticationToken.class);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn("google");

        OidcUser oidcUser = mock(OidcUser.class);
        when(authentication.getPrincipal()).thenReturn(oidcUser);

        when(oidcUser.getSubject()).thenReturn("google-sub-123");
        when(oidcUser.getEmail()).thenReturn("a@b.com");
        when(oidcUser.getEmailVerified()).thenReturn(true);
        when(oidcUser.getFullName()).thenReturn("Alice");
        when(oidcUser.getPicture()).thenReturn("https://img.example/x.png");

        // provision -> user
        User user = new User();
        user.setId(777L);
        user.setRole(Role.USER);

        when(provisionService.provision(any(OAuth2UserDto.class))).thenReturn(user);

        // refresh issue
        when(refreshTokenService.issueNew(eq(user), eq("JUnit-UA"), eq("203.0.113.10")))
                .thenReturn(new IssueNewResult("raw.refresh.token", 55L));

        // access issue
        when(accessTokenService.createAccessToken(eq(777L), anyList()))
                .thenReturn("access.jwt");

        // act
        handler.onAuthenticationSuccess(request, response, authentication);

        // verify dto passed into provision (перевіряємо, що реально збирається з OIDC)
        verify(provisionService).provision(oauthDtoCaptor.capture());
        OAuth2UserDto dto = oauthDtoCaptor.getValue();

        assertThat(dto.providerId()).isEqualTo("google-sub-123");
        assertThat(dto.email()).isEqualTo("a@b.com");
        assertThat(dto.emailVerified()).isTrue();
        assertThat(dto.name()).isEqualTo("Alice");
        assertThat(dto.picture()).isEqualTo("https://img.example/x.png");

        // verify tokens + cookie
        verify(refreshTokenService).issueNew(eq(user), eq("JUnit-UA"), eq("203.0.113.10"));
        verify(accessTokenService).createAccessToken(eq(777L), rolesCaptor.capture());
        assertThat(rolesCaptor.getValue()).containsExactly("USER");
        verify(cookieService).setRefreshCookie(eq(response), eq("raw.refresh.token"));

        // verify response
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(response.getCharacterEncoding()).isEqualTo(StandardCharsets.UTF_8.name());
        assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);

        JsonNode json = objectMapper.readTree(response.getContentAsByteArray());

        // поля залежать від AuthResponse. Тут перевірка за змістом:
        assertThat(json.get("accessToken").asText()).isEqualTo("access.jwt");
        assertThat(json.get("tokenType").asText()).isEqualTo("Bearer");
        assertThat(json.get("expiresInSeconds").asLong()).isEqualTo(600L);
        assertThat(json.get("userId").asLong()).isEqualTo(777L);
        assertThat(json.get("role").asText()).isEqualTo("USER");
    }

    @Test
    void onAuthenticationSuccess_whenPrincipalNotOidcUser_shouldThrowClassCastException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        OAuth2User notOidc = mock(OAuth2User.class); // НЕ OidcUser

        OAuth2AuthenticationToken authentication = mock(OAuth2AuthenticationToken.class);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn("google");
        when(authentication.getPrincipal()).thenReturn(notOidc);

        assertThatThrownBy(() -> handler.onAuthenticationSuccess(request, response, authentication))
                .isInstanceOf(ClassCastException.class);

        // нічого не повинно бути випущено
        verifyNoInteractions(provisionService, accessTokenService, refreshTokenService, cookieService);
    }
}
