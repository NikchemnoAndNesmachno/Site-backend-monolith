package ua.nin.identity.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ua.nin.identity.auth.config.JwtConfig;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JwtConfig.class, AccessTokenService.class})
@TestPropertySource(properties = {
        "jwt.secret=dev-secret-change-measdasdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111",
        "jwt.issuer=https://test",
        "jwt.access-ttl-minutes=1"
})
class AccessTokenServiceTest {

    @Autowired
    private AccessTokenService accessTokenService;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Test
    void createAccessToken_validToken() {
        String token = accessTokenService.createAccessToken(10L, List.of("USER"));
        Jwt decoded = jwtDecoder.decode(token);

        assertEquals("10", decoded.getSubject());
        assertEquals("https://test", decoded.getIssuer().toString());
        assertEquals(List.of("USER"), decoded.getClaimAsStringList("roles"));
    }

    @Test
    void decode_expiredToken_throws() {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("https://test")
                .subject("1")
                .issuedAt(now.minusSeconds(120))
                .expiresAt(now.minusSeconds(60))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        String token = jwtEncoder
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();

        assertThrows(Exception.class, () -> jwtDecoder.decode(token));
    }

    @Test
    void decode_invalidToken_throws() {
        String invalidToken = "invalid.token.value";

        assertThrows(Exception.class, () -> jwtDecoder.decode(invalidToken));
    }
}