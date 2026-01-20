package ua.nin.identity.auth.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class TimeTokenUtils {

    private final SecureRandom secureRandom;

    @Value("${security.ott.bytes:48}")
    private int bytes;

    @Value("${security.refresh.pepper:CHANGE_ME}")
    private String pepper;

    public String generateRawToken() {
        byte[] buf = new byte[bytes];
        secureRandom.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    /*
    Hash:
    SHA-256(raw + serverPepper) або HMAC-SHA256.
     */
    public String hash(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // pepper робимо “серверною сіллю”, щоб витік БД не давав швидко брутити
            byte[] input = (raw + "." + pepper).getBytes(StandardCharsets.UTF_8);
            byte[] digest = md.digest(input);
            return new String(Hex.encode(digest));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot hash token", e);
        }
    }
}
