package ua.nin.identity.auth.config;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;
import ua.nin.identity.auth.oauth2.state.CookieAuthorizationRequestRepository;
import ua.nin.identity.auth.service.HttpCookieService;
import ua.nin.identity.auth.oauth2.handler.OAuth2SuccessHandler;
import ua.nin.identity.auth.util.TimeTokenUtils;

import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static ua.nin.common.constant.StringEndpoints.*;
import static ua.nin.common.constant.AppConstant.ADMIN;
import static ua.nin.common.constant.AppConstant.USER;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final TimeTokenUtils timeTokenUtils;
    private final HttpCookieService httpCookieService;

    @Bean
    public RequestMatcher publicMatcher() {
        var p = PathPatternRequestMatcher.withDefaults();

        return new OrRequestMatcher(
                p.matcher(HttpMethod.POST, API_V1_AUTH_REGISTER),
                p.matcher(HttpMethod.POST, API_V1_AUTH_LOGIN),
                p.matcher(HttpMethod.POST, API_V1_AUTH_LOGOUT),
                p.matcher(HttpMethod.POST, API_V1_AUTH_REFRESH),

                p.matcher(HttpMethod.GET, API_V1_AUTH_EMAIL_VERIFY),
                p.matcher(HttpMethod.POST, API_V1_AUTH_EMAIL_RESEND),

                p.matcher(HttpMethod.POST, API_V1_AUTH_PASSWORD_FORGOT),
                p.matcher(HttpMethod.POST, API_V1_AUTH_PASSWORD_RESET),

                p.matcher(HttpMethod.GET, API_V1_USERS_BY_USERNAME),

                p.matcher(HttpMethod.GET, API_V1_MEDIA_BY_ID),
                p.matcher(HttpMethod.GET, API_V1_MEDIA_BY_ID_META),
                p.matcher(HttpMethod.GET, API_V1_VIDEO_BY_ID),

                p.matcher(HttpMethod.GET, API_V1_REACTIONS_BY_TARGET_TYPE_BY_TARGET_ID_COUNTS),

                p.matcher(HttpMethod.GET, API_V1_COMMENTS),
                p.matcher(HttpMethod.GET, API_V1_COMMENTS_BY_PARENT_ID_REPLIES),

                p.matcher(HttpMethod.GET, API_V1_VIEWS),
                p.matcher(HttpMethod.POST, API_V1_VIEWS),

                p.matcher(HttpMethod.GET, API_V1_FEED),

                p.matcher(HttpMethod.GET, "/login/oauth2/code/**"),
                p.matcher(HttpMethod.GET, "/oauth2/authorization/**"),

                p.matcher(ACTUATOR),
                p.matcher(V3_API_DOCS),
                p.matcher(SWAGGER_UI),
                p.matcher(SWAGGER_UI_HTML),

                p.matcher(HttpMethod.OPTIONS, "/**"),
                p.matcher("/error")
        );
    }

    @Bean
    @Order(1)
    public SecurityFilterChain publicChain(HttpSecurity http, RequestMatcher publicMatcher, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http.securityMatcher(publicMatcher)
                // REST API -> CSRF зазвичай вимикають, бо немає server-side session.
                // Refresh cookie в такій схемі теж ок, але якщо хочеш "максимально строго" —
                // можемо додати CSRF token тільки для /auth/refresh.

                .cors(corsCustomizer -> corsCustomizer.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a.anyRequest().permitAll())
                .oauth2Login(o -> o
                        .authorizationEndpoint(ae -> ae
                                .authorizationRequestRepository(authorizationRequestRepository())
                        )
                        .successHandler(oAuth2SuccessHandler)
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter))
                );

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain protectedChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .cors(corsCustomizer -> corsCustomizer.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exception -> exception.authenticationEntryPoint((req, resp, exc) -> resp
                                .sendError(SC_UNAUTHORIZED, "Authorize first."))
                        .accessDeniedHandler((req, resp, exc) -> resp.sendError(SC_FORBIDDEN, "You don't have authorities.")))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()
                        // --- Protected endpoints ---
                        .requestMatchers(HttpMethod.GET, API_V1_AUTH_ME).hasAnyRole(USER, ADMIN)
                        .requestMatchers(HttpMethod.POST, API_V1_AUTH_LOGOUT_ALL).hasAnyRole(USER, ADMIN)

                        .requestMatchers(HttpMethod.POST, API_V1_AUTH_PASSWORD_CHANGE).hasAnyRole(USER, ADMIN)

                        .requestMatchers(HttpMethod.GET, API_V1_USERS_ME).hasAnyRole(USER, ADMIN)
                        .requestMatchers(HttpMethod.PATCH, API_V1_USERS_ME).hasAnyRole(USER, ADMIN)

                        .requestMatchers(HttpMethod.POST, API_V1_MEDIA_UPLOAD).hasAnyRole(USER, ADMIN)
                        .requestMatchers(HttpMethod.DELETE, API_V1_MEDIA_BY_ID).hasAnyRole(ADMIN)

                        .requestMatchers(HttpMethod.POST, API_V1_AVATAR_UPLOAD).hasAnyRole(USER, ADMIN)
                        .requestMatchers(HttpMethod.DELETE, API_V1_AVATAR_BY_ID).hasAnyRole(USER, ADMIN)

                        .requestMatchers(HttpMethod.POST, API_V1_VIDEO_UPLOAD_WITH_PREVIEW).hasAnyRole(USER, ADMIN)
                        .requestMatchers(HttpMethod.DELETE, API_V1_VIDEO_BY_ID).hasAnyRole(USER, ADMIN)

                        .requestMatchers(HttpMethod.PUT, API_V1_REACTIONS).hasAnyRole(USER, ADMIN)
                        .requestMatchers(HttpMethod.GET, API_V1_REACTIONS_BY_TARGET_TYPE_BY_TARGET_ID_MY).hasAnyRole(USER, ADMIN)

                        .requestMatchers(HttpMethod.POST, API_V1_COMMENTS).hasAnyRole(USER, ADMIN)
                        .requestMatchers(HttpMethod.PATCH, API_V1_COMMENTS_BY_ID).hasAnyRole(USER, ADMIN)
                        .requestMatchers(HttpMethod.DELETE, API_V1_COMMENTS_BY_ID).hasAnyRole(USER, ADMIN)

                        // все інше — за замовчуванням закрите
                        .anyRequest().hasAnyRole(ADMIN)
                );
                // JWT access token validation
//                .oauth2ResourceServer(oauth2 -> oauth2
//                        .jwt(jwt -> jwt.decoder(jwtDecoder)
//                                .jwtAuthenticationConverter(jwtAuthenticationConverter))
//                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
        return new CookieAuthorizationRequestRepository(timeTokenUtils, httpCookieService);
    }
}
