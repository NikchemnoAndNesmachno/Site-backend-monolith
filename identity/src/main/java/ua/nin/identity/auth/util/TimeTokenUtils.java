package ua.nin.identity.auth.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class TimeTokenUtils {

    private final SecureRandom secureRandom;
    private final int bytes;
    private final String pepper;
    private final String hmacSecret;

    public TimeTokenUtils(SecureRandom secureRandom,
                          @Value("${security.ott.bytes:32}") int bytes,
                          @Value("${security.ott.pepper:CHANGE_ME}") String pepper,
                          @Value("${jwt.secret}") String hmacSecret) {
        this.secureRandom = secureRandom;
        this.bytes = bytes;
        this.pepper = pepper;
        this.hmacSecret = hmacSecret;
    }


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

    public String hmacSha256(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(hmacSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] out = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(out);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot hash token", e);
        }
    }
}
