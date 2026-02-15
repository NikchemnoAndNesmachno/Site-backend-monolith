package ua.nin.identity.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import ua.nin.identity.auth.service.OAuth2SuccessHandler;

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

                p.matcher(HttpMethod.GET, API_V1_REACTIONS_BY_TARGET_TYPE_BY_TARGET_ID_COUNTS),

                p.matcher(HttpMethod.GET, API_V1_COMMENTS),
                p.matcher(HttpMethod.GET, API_V1_COMMENTS_BY_PARENT_ID_REPLIES),

                p.matcher(HttpMethod.GET, API_V1_VIEWS),
                p.matcher(HttpMethod.POST, API_V1_VIEWS),

                p.matcher(HttpMethod.GET, "/login/oauth2/code/**"),
                p.matcher(HttpMethod.GET, "/oauth2/authorization/**"),

                p.matcher(ACTUATOR),
                p.matcher(V3_API_DOCS),
                p.matcher(SWAGGER_UI),
                p.matcher(SWAGGER_UI_HTML),

                p.matcher(HttpMethod.OPTIONS, "/**")
        );
    }

    @Bean
    @Order(1)
    public SecurityFilterChain publicChain(HttpSecurity http, RequestMatcher publicMatcher) throws Exception {
        http.securityMatcher(publicMatcher)
                // REST API -> CSRF зазвичай вимикають, бо немає server-side session.
                // Refresh cookie в такій схемі теж ок, але якщо хочеш "максимально строго" —
                // можемо додати CSRF token тільки для /auth/refresh.

                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(a -> a.anyRequest().permitAll())
                .oauth2Login(o -> o
                        .authorizationEndpoint(ae -> ae
                                .authorizationRequestRepository(authorizationRequestRepository())
                        )
                        .successHandler(oAuth2SuccessHandler)
                );

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain protectedChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
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
                )
                // JWT access token validation
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter))
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository();
    }
}
