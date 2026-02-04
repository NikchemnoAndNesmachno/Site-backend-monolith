package ua.nin.common.util;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.*;

@Component
@NoArgsConstructor
public final class StringHelperUtils {

    public static String normalizeEmail(String email) {
        String trimmed = trimToNull(email);
        return toRootLowerCase(trimmed);
    }

    public static String normalizeUsername(String username) {
        String trimmed = trimToNull(username);
        return toRootLowerCase(trimmed);
    }

    public static String normalizeContentType(String contentType) {
        String trimmed = trimToNull(contentType);
        return toRootLowerCase(trimmed);
    }

    public static String normalizeAndTruncate(String s, int maxLength) {
        String t = trimToNull(s);
        if (t == null) return null;
        return t.length() <= maxLength ? t : t.substring(0, maxLength);
    }

    public static String normalizeTargetType(String targetType) {
        String t = trimToNull(targetType);
        return toRootUpperCase(t);
    }

    public static String normalizeReactionCode(String reactionCode) {
        String t = trimToNull(reactionCode);
        return toRootUpperCase(t);
    }

    public static String normalizeBody(String body) {
        String t = trimToEmpty(body);
        return t.length() > 500
                ? t.substring(0, 500)
                : t;
    }
}
