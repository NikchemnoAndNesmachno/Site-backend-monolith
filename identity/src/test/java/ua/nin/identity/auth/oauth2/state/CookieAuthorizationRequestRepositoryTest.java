package ua.nin.identity.auth.oauth2.state;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import ua.nin.identity.auth.exception.exceptions.CookieDeserializeException;
import ua.nin.identity.auth.exception.exceptions.CookieSerializeException;
import ua.nin.identity.auth.service.HttpCookieService;
import ua.nin.identity.auth.util.TimeTokenUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static ua.nin.identity.auth.oauth2.state.CookieAuthorizationRequestRepository.*;

@ExtendWith(MockitoExtension.class)
class CookieAuthorizationRequestRepositoryTest {

    @Mock private TimeTokenUtils timeTokenUtils;
    @Mock private HttpCookieService httpCookieService;

    @Captor private ArgumentCaptor<String> cookieValueCaptor;

    private CookieAuthorizationRequestRepository repo;

    @BeforeEach
    void setUp() {
        repo = new CookieAuthorizationRequestRepository(timeTokenUtils, httpCookieService);
    }

    // Стабим только там, где реально вызывается hmacSha256()
    private void stubDeterministicHmac() {
        when(timeTokenUtils.hmacSha256(anyString()))
                .thenAnswer(inv -> sha256Base64Url(inv.getArgument(0, String.class)));
    }

    @Test
    void saveAuthorizationRequest_shouldSetAuthRequestCookie_andRedirectCookie() {
        stubDeterministicHmac();

        var req = new MockHttpServletRequest();
        var resp = new MockHttpServletResponse();

        OAuth2AuthorizationRequest authReq = sampleAuthRequestWithRedirect("https://front/app/after");

        repo.saveAuthorizationRequest(authReq, req, resp);

        verify(httpCookieService).addCookie(
                resp,
                OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
                cookieValueCaptor.capture(),
                OAUTH2_PATH,
                180L // <-- ВАЖНО: long
        );

        String serialized = cookieValueCaptor.getValue();
        assertThat(serialized).contains(".");
        assertThat(serialized.split("\\.")).hasSize(2);

        verify(httpCookieService).addCookie(
                resp,
                REDIRECT_URI_COOKIE_NAME,
                "https://front/app/after",
                OAUTH2_PATH,
                180L // <-- long
        );

        verify(httpCookieService, never()).deleteCookie(any(), anyString(), anyString());
    }

    @Test
    void saveAuthorizationRequest_whenNull_shouldDeleteCookies() {
        var req = new MockHttpServletRequest();
        var resp = new MockHttpServletResponse();

        repo.saveAuthorizationRequest(null, req, resp);

        verify(httpCookieService).deleteCookie(resp, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, OAUTH2_PATH);
        verify(httpCookieService).deleteCookie(resp, REDIRECT_URI_COOKIE_NAME, OAUTH2_PATH);
        verify(httpCookieService, never()).addCookie(any(), anyString(), anyString(), anyString(), anyLong());

        // и вот тут hmacSha256 НЕ вызывается -> поэтому не надо его стабать в setUp()
        verifyNoInteractions(timeTokenUtils);
    }

    @Test
    void loadAuthorizationRequest_shouldReturnDeserializedRequest() {
        stubDeterministicHmac();

        var req = new MockHttpServletRequest();

        OAuth2AuthorizationRequest original = sampleAuthRequestWithRedirect("https://front/app/after");
        String serialized = repo.serialize(original);

        when(httpCookieService.getCookie(req, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME))
                .thenReturn(Optional.of(new Cookie(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, serialized)));

        OAuth2AuthorizationRequest loaded = repo.loadAuthorizationRequest(req);

        assertThat(loaded).isNotNull();
        assertThat(loaded.getAuthorizationUri()).isEqualTo(original.getAuthorizationUri());
        assertThat(loaded.getClientId()).isEqualTo(original.getClientId());
        assertThat(loaded.getRedirectUri()).isEqualTo(original.getRedirectUri());
        assertThat(loaded.getState()).isEqualTo(original.getState());
        assertThat(loaded.getScopes()).containsExactlyInAnyOrderElementsOf(original.getScopes());

        assertThat(loaded.getAdditionalParameters())
                .containsEntry(REDIRECT_URI_COOKIE_NAME, "https://front/app/after")
                .containsEntry("access_type", "offline");

        assertThat(loaded.getAttributes()).containsEntry("k", "v");
    }

    @Test
    void loadAuthorizationRequest_whenNoCookie_shouldReturnNull() {
        var req = new MockHttpServletRequest();

        when(httpCookieService.getCookie(req, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME))
                .thenReturn(Optional.empty());

        assertThat(repo.loadAuthorizationRequest(req)).isNull();
        verifyNoInteractions(timeTokenUtils);
    }

    @Test
    void removeAuthorizationRequest_shouldReturnRequest_andDeleteCookies() {
        stubDeterministicHmac();

        var req = new MockHttpServletRequest();
        var resp = new MockHttpServletResponse();

        OAuth2AuthorizationRequest original = sampleAuthRequestWithRedirect("https://front/app/after");
        String serialized = repo.serialize(original);

        when(httpCookieService.getCookie(req, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME))
                .thenReturn(Optional.of(new Cookie(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, serialized)));

        OAuth2AuthorizationRequest removed = repo.removeAuthorizationRequest(req, resp);

        assertThat(removed).isNotNull();
        assertThat(removed.getClientId()).isEqualTo(original.getClientId());

        verify(httpCookieService).deleteCookie(resp, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, OAUTH2_PATH);
        verify(httpCookieService).deleteCookie(resp, REDIRECT_URI_COOKIE_NAME, OAUTH2_PATH);
    }

    @Test
    void deserialize_whenInvalidFormat_shouldThrowCookieDeserializeException() {
        assertThatThrownBy(() -> repo.deserialize("no-dot-here", OAuth2AuthorizationRequest.class))
                .isInstanceOf(CookieDeserializeException.class);

        verifyNoInteractions(timeTokenUtils);
    }

    @Test
    void deserialize_whenTamperedData_shouldThrowCookieDeserializeException() {
        stubDeterministicHmac();

        OAuth2AuthorizationRequest original = sampleAuthRequestWithRedirect("https://front/app/after");
        String serialized = repo.serialize(original);

        String[] parts = serialized.split("\\.");
        assertThat(parts).hasSize(2);

        String data = parts[0];
        String sig = parts[1];

        String tamperedData = flipLastChar(data);
        String tampered = tamperedData + "." + sig;

        assertThatThrownBy(() -> repo.deserialize(tampered, OAuth2AuthorizationRequest.class))
                .isInstanceOf(CookieDeserializeException.class);
    }

    @Test
    void serialize_whenHmacThrows_shouldThrowCookieSerializeException() {
        when(timeTokenUtils.hmacSha256(anyString()))
                .thenThrow(new RuntimeException("boom"));

        OAuth2AuthorizationRequest original = sampleAuthRequestWithRedirect("https://front/app/after");

        assertThatThrownBy(() -> repo.serialize(original))
                .isInstanceOf(CookieSerializeException.class);
    }

    // ---- helpers ----

    private static OAuth2AuthorizationRequest sampleAuthRequestWithRedirect(String redirectCookieValue) {
        return OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .clientId("client-123")
                .redirectUri("https://api.nin.ua/login/oauth2/code/google")
                .scopes(Set.of("openid", "email", "profile"))
                .state("state-xyz")
                .attributes(Map.of("k", "v"))
                .additionalParameters(Map.of(
                        REDIRECT_URI_COOKIE_NAME, redirectCookieValue,
                        "access_type", "offline"
                ))
                .build();
    }

    private static String sha256Base64Url(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String flipLastChar(String s) {
        if (s == null || s.isEmpty()) return "A";
        char last = s.charAt(s.length() - 1);
        char repl = (last == 'A') ? 'B' : 'A';
        return s.substring(0, s.length() - 1) + repl;
    }
}
