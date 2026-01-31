package ua.nin.media.util;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ua.nin.media.model.MediaKind;

import java.time.Instant;
import java.util.UUID;

@Component
@NoArgsConstructor
public final class MediaUtils {

    public static MediaKind detectKind(String contentType) {
        if (contentType == null) return MediaKind.OTHER;
        if (contentType.equals("image/gif")) return MediaKind.GIF;
        if (contentType.startsWith("image/")) return MediaKind.IMAGE;
        if (contentType.startsWith("video/")) return MediaKind.VIDEO;
        return MediaKind.OTHER;
    }

    public static String sanitizeOriginalFilename(String originalFilename) {
        String f = StringUtils.trimToEmpty(originalFilename);
        if (f.isEmpty()) return null;

        // strip any path segments
        f = f.replace("\\", "/");
        int slash = f.lastIndexOf('/');
        if (slash >= 0) f = f.substring(slash + 1);

        // hard truncate
        if (f.length() > 255) f = f.substring(0, 255);
        return f;
    }

    public static String pickExtension(String originalFilename, String contentType) {
        // from filename
        if (originalFilename != null) {
            int dot = originalFilename.lastIndexOf('.');
            if (dot >= 0 && dot < originalFilename.length() - 1) {
                String ext = originalFilename.substring(dot + 1).trim().toLowerCase();
                if (ext.matches("[a-z0-9]{1,8}")) return ext;
            }
        }

        // fallback mapping
        if (contentType == null) return "bin";
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            case "video/mp4" -> "mp4";
            case "video/webm" -> "webm";
            default -> "bin";
        };
    }

    public static String buildTmpStorageKey(String extension) {
        Instant now = Instant.now();
        String date = now.toString().substring(0, 10).replace('-', '/'); // yyyy/MM/dd
        String id = UUID.randomUUID().toString().replace("-", "");
        String ext = (extension == null || extension.isBlank()) ? "" : ("." + extension);
        return "tmp/media/" + date + "/" + id + ext;
    }

    public static String buildStorageKey(String extension, String sha256) {
        Instant now = Instant.now();
        String date = now.toString().substring(0, 10).replace('-', '/'); // yyyy/MM/dd
        String ext = (extension == null || extension.isBlank()) ? "" : ("." + extension);
        return "media/" + date + "/" + sha256 + ext;
    }
}
