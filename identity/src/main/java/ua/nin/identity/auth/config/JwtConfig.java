package ua.nin.identity.auth.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class JwtConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.secret-base64:false}")
    private boolean secretBase64;

    @Value("${jwt.issuer:NiN}")
    private String issuer;

    @Bean
    public SecretKey jwtSecretKey() {
        byte[] secretBytes = secretBase64
                ? Base64.getDecoder().decode(jwtSecret)
                : jwtSecret.getBytes(StandardCharsets.UTF_8);

        if (secretBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret is too short for HS256 (min 32 bytes)"
            );
        }

        return new SecretKeySpec(secretBytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(
                new ImmutableSecret<>(jwtSecretKey)
        );
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(jwtSecretKey)
                .build();

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);

        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        withIssuer
                ));

        return decoder;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter conv = new JwtAuthenticationConverter();

        conv.setJwtGrantedAuthoritiesConverter(jwt -> {
            // 1) prefer "roles": ["USER","ADMIN"]
            List<String> roles = jwt.getClaimAsStringList("roles");

            // 2) fallback: "role": "USER" or ["USER"]
            if (roles == null) {
                roles = jwt.getClaimAsStringList("role");
            }
            if (roles == null) {
                String single = jwt.getClaimAsString("role");
                roles = single == null ? List.of() : List.of(single);
            }

            return roles.stream()
                    .filter(r -> r != null && !r.isBlank())
                    .map(String::trim)
                    .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                    .map(r -> (GrantedAuthority) new SimpleGrantedAuthority(r))
                    .collect(Collectors.toSet());
        });

        return conv;
    }
}
