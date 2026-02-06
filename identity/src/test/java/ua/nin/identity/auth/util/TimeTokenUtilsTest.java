package ua.nin.identity.auth.util;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.*;

class TimeTokenUtilsTest {

    @Test
    void generateRawToken_uniqueAndNonEmpty() {
        TimeTokenUtils utils = new TimeTokenUtils(new SecureRandom());
        ReflectionTestUtils.setField(utils, "bytes", 32);
        ReflectionTestUtils.setField(utils, "pepper", "pepper");

        String token1 = utils.generateRawToken();
        String token2 = utils.generateRawToken();

        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);
    }

    @Test
    void hash_isDeterministicAndNotRaw() {
        TimeTokenUtils utils = new TimeTokenUtils(new SecureRandom());
        ReflectionTestUtils.setField(utils, "pepper", "pepper");

        String hash1 = utils.hash("raw");
        String hash2 = utils.hash("raw");

        assertEquals(hash1, hash2);
        assertNotEquals("raw", hash1);
    }
}
