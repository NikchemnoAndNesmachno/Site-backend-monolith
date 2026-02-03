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
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    @Bean
    public RequestMatcher publicMatcher() {
        var p = PathPatternRequestMatcher.withDefaults();

        return new OrRequestMatcher(
                p.matcher(HttpMethod.POST, "/api/v1/auth/register"),
                p.matcher(HttpMethod.POST, "/api/v1/auth/login"),
                p.matcher(HttpMethod.POST, "/api/v1/auth/logout"),
                p.matcher(HttpMethod.POST, "/api/v1/auth/refresh"),
                p.matcher(HttpMethod.POST, "/api/v1/auth/email/verify"),
                p.matcher(HttpMethod.POST, "/api/v1/auth/email/resend"),
                p.matcher(HttpMethod.POST, "/api/v1/auth/password/forgot"),
                p.matcher(HttpMethod.POST, "/api/v1/auth/password/reset"),

                p.matcher(HttpMethod.GET, "/api/v1/auth/media/*"),

                // будь-який
                p.matcher("/actuator/**"),
                p.matcher("/v3/api-docs/**"),
                p.matcher("/swagger-ui/**"),
                p.matcher("/swagger-ui.html"),

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
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a.anyRequest().permitAll());

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
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/me").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout-all").hasAnyRole("USER", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/password/change").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/v1/auth/sessions/**").hasAnyRole("USER", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/v1/video/upload/video-with-preview").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/media/upload").hasAnyRole("USER", "ADMIN")
                        // все інше — за замовчуванням закрите
                        .anyRequest().hasAnyRole("USER", "ADMIN")
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
}
