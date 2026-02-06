package ua.nin.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringHelperUtilsTest {

    @Test
    void normalizeEmail_trimsAndLowercases() {
        assertEquals("user@example.com", StringHelperUtils.normalizeEmail("  USER@Example.com "));
        assertNull(StringHelperUtils.normalizeEmail("  "));
    }

    @Test
    void normalizeUsername_trimsAndLowercases() {
        assertEquals("user_name", StringHelperUtils.normalizeUsername("  UsEr_Name  "));
        assertNull(StringHelperUtils.normalizeUsername(null));
    }

    @Test
    void normalizeContentType_lowercases() {
        assertEquals("image/png", StringHelperUtils.normalizeContentType(" IMAGE/PNG "));
        assertNull(StringHelperUtils.normalizeContentType(null));
    }

    @Test
    void normalizeAndTruncate_limitsLength() {
        assertEquals("short", StringHelperUtils.normalizeAndTruncate(" short ", 10));
        assertEquals("long", StringHelperUtils.normalizeAndTruncate("longvalue", 4));
        assertNull(StringHelperUtils.normalizeAndTruncate("   ", 5));
    }

    @Test
    void normalizeTargetType_uppercases() {
        assertEquals("VIDEO", StringHelperUtils.normalizeTargetType(" video "));
        assertNull(StringHelperUtils.normalizeTargetType(null));
    }

    @Test
    void normalizeReactionCode_uppercases() {
        assertEquals("LIKE", StringHelperUtils.normalizeReactionCode(" like "));
        assertNull(StringHelperUtils.normalizeReactionCode(null));
    }

    @Test
    void normalizeBody_truncatesTo500() {
        String body = "a".repeat(600);
        String normalized = StringHelperUtils.normalizeBody(body);

        assertEquals(500, normalized.length());
    }

    @Test
    void normalizeBody_truncatesToEmpty() {
        String body = "";
        String normalizedNull = StringHelperUtils.normalizeBody(body);

        assertEquals("", normalizedNull);
    }
}
