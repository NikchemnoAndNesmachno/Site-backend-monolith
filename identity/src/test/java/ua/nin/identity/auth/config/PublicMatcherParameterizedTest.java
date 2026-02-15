package ua.nin.identity.auth.config;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import ua.nin.identity.auth.service.OAuth2SuccessHandler;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static ua.nin.common.constant.StringEndpoints.*;

class PublicMatcherParameterizedTest {

    @Mock
    private JwtDecoder jwtDecoder;
    @Mock
    private JwtAuthenticationConverter jwtAuthenticationConverter;
    @Mock
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    // publicMatcher() не використовує jwtDecoder/converter, тому можна надати заглушки.
    private final RequestMatcher publicMatcher = new SecurityConfig(
            jwtDecoder,
            jwtAuthenticationConverter,
            oAuth2SuccessHandler
    ).publicMatcher();

    // ---------- helpers ----------
    private static MockHttpServletRequest req(String method, String uri) {
        // В Spring Security для matcher'ів важливі method + requestURI/servletPath
        var r = new MockHttpServletRequest(method, uri);
        r.setServletPath(uri);
        return r;
    }

    // ---------- positive cases (must match) ----------
    static Stream<Case> publicEndpoints() {
        return Stream.of(
                // auth
                c(HttpMethod.POST, API_V1_AUTH_REGISTER, "/api/v1/auth/register"),
                c(HttpMethod.POST, API_V1_AUTH_LOGIN, "/api/v1/auth/login"),
                c(HttpMethod.POST, API_V1_AUTH_LOGOUT, "/api/v1/auth/logout"),
                c(HttpMethod.POST, API_V1_AUTH_REFRESH, "/api/v1/auth/refresh"),

                c(HttpMethod.GET, API_V1_AUTH_EMAIL_VERIFY, "/api/v1/auth/email/verify"),
                c(HttpMethod.POST, API_V1_AUTH_EMAIL_RESEND, "/api/v1/auth/email/resend"),

                c(HttpMethod.POST, API_V1_AUTH_PASSWORD_FORGOT, "/api/v1/auth/password/forgot"),
                c(HttpMethod.POST, API_V1_AUTH_PASSWORD_RESET, "/api/v1/auth/password/reset"),

                // users
                c(HttpMethod.GET, API_V1_USERS_BY_USERNAME, "/api/v1/users/by-username/ivan"),

                // media
                c(HttpMethod.GET, API_V1_MEDIA_BY_ID, "/api/v1/media/123"),
                c(HttpMethod.GET, API_V1_MEDIA_BY_ID_META, "/api/v1/media/123/meta"),

                // reactions
                c(HttpMethod.GET, API_V1_REACTIONS_BY_TARGET_TYPE_BY_TARGET_ID_COUNTS, "/api/v1/reactions/post/10/counts"),

                // comments
                c(HttpMethod.GET, API_V1_COMMENTS, "/api/v1/comments"),
                c(HttpMethod.GET, API_V1_COMMENTS_BY_PARENT_ID_REPLIES, "/api/v1/comments/777/replies"),

                // views
                c(HttpMethod.GET, API_V1_VIEWS, "/api/v1/views"),
                c(HttpMethod.POST, API_V1_VIEWS, "/api/v1/views"),

                // OAuth2
                c(HttpMethod.GET, "/login/oauth2/code/**", "/login/oauth2/code/google"),
                c(HttpMethod.GET, "/oauth2/authorization/**", "/oauth2/authorization/google"),

                // actuator + swagger
                c(null, ACTUATOR, "/actuator/health"),
                c(null, V3_API_DOCS, "/v3/api-docs"),
                c(null, V3_API_DOCS, "/v3/api-docs/swagger-config"),
                c(null, SWAGGER_UI, "/swagger-ui/index.html"),
                c(null, SWAGGER_UI_HTML, "/swagger-ui.html"),

                // cors preflight
                c(HttpMethod.OPTIONS, "/**", "/anything/here")
        );
    }

    @ParameterizedTest(name = "publicMatcher should match: {0}")
    @MethodSource("publicEndpoints")
    void shouldMatchPublicEndpoints(Case tc) {
        var method = (tc.method == null ? "GET" : tc.method.name()); // для безметодных matcher'ов можно любой метод
        assertThat(publicMatcher.matches(req(method, tc.exampleUri))).isTrue();
    }

    // ---------- negative cases (must NOT match) ----------
    static Stream<Case> mustNotBePublic() {
        return Stream.of(
                // критичный кейс перетину: /users/me НЕ повинен стати публічний через /users/*
                c(HttpMethod.GET, API_V1_USERS_ME, "/api/v1/users/me"),

                // приватні auth
                c(HttpMethod.GET, API_V1_AUTH_ME, "/api/v1/auth/me"),
                c(HttpMethod.POST, API_V1_AUTH_LOGOUT_ALL, "/api/v1/auth/logout-all"),

                // change password (priv)
                c(HttpMethod.POST, API_V1_AUTH_PASSWORD_CHANGE, "/api/v1/auth/password/change"),

                // uploads priv
                c(HttpMethod.POST, API_V1_MEDIA_UPLOAD, "/api/v1/media/upload"),
                c(HttpMethod.POST, API_V1_AVATAR_UPLOAD, "/api/v1/avatar/upload"),
                c(HttpMethod.POST, API_V1_VIDEO_UPLOAD_WITH_PREVIEW, "/api/v1/video/upload/video-with-preview"),

                // reactions priv
                c(HttpMethod.PUT, API_V1_REACTIONS, "/api/v1/reactions"),
                c(HttpMethod.GET, API_V1_REACTIONS_BY_TARGET_TYPE_BY_TARGET_ID_MY, "/api/v1/reactions/post/10/my"),

                // comments priv methods
                c(HttpMethod.POST, API_V1_COMMENTS, "/api/v1/comments"),
                c(HttpMethod.PATCH, API_V1_COMMENTS_BY_ID, "/api/v1/comments/123"),
                c(HttpMethod.DELETE, API_V1_COMMENTS_BY_ID, "/api/v1/comments/123")
        );
    }

    @ParameterizedTest(name = "publicMatcher should NOT match: {0}")
    @MethodSource("mustNotBePublic")
    void shouldNotMatchPrivateEndpoints(Case tc) {
        assertThat(publicMatcher.matches(req(tc.method.name(), tc.exampleUri))).isFalse();
    }

    // ---------- wrong method should NOT match ----------
    static Stream<WrongMethodCase> wrongMethods() {
        return Stream.of(
                // login/register тільки POST
                wm(HttpMethod.GET, "/api/v1/auth/login"),
                wm(HttpMethod.PUT, "/api/v1/auth/register"),

                // users/* тільки GET
                wm(HttpMethod.POST, "/api/v1/users/ivan"),

                // views: GET і POST дозволені, а PUT — ні
                wm(HttpMethod.PUT, "/api/v1/views"),

                // comments GET дозволений, POST — ні (POST захищен)
                wm(HttpMethod.POST, "/api/v1/comments")
        );
    }

    @ParameterizedTest(name = "publicMatcher should NOT match wrong method: {0}")
    @MethodSource("wrongMethods")
    void shouldNotMatchWrongHttpMethod(WrongMethodCase tc) {
        assertThat(publicMatcher.matches(req(tc.method.name(), tc.uri))).isFalse();
    }

    // ---------- small records ----------
    record Case(HttpMethod method, String pattern, String exampleUri) {
        @Override public String toString() { return (method == null ? "*" : method) + " " + pattern + " ~ " + exampleUri; }
    }

    record WrongMethodCase(HttpMethod method, String uri) {
        @Override public String toString() { return method + " " + uri; }
    }

    private static Case c(HttpMethod method, String pattern, String exampleUri) {
        return new Case(method, pattern, exampleUri);
    }

    private static WrongMethodCase wm(HttpMethod method, String uri) {
        return new WrongMethodCase(method, uri);
    }
}
