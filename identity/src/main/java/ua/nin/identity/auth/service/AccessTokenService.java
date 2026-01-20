package ua.nin.identity.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccessTokenService {

    private final JwtEncoder jwtEncoder;

    @Value("${jwt.issuer:NiN}")
    private String issuer;

    @Value("${jwt.access-ttl-minutes:10}")
    private long accessTtlMinutes;

    public String createAccessToken(long userId, List<String> roles) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTtlMinutes * 60);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer) // iss
                .issuedAt(now) // iat
                .expiresAt(exp) // exp
                .subject(String.valueOf(userId)) // sub=userId
                .claim("roles", roles) // roles=[...]
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }
}
