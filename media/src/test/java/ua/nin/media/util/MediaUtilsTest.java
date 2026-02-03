package ua.nin.media.util;

import org.junit.jupiter.api.Test;
import ua.nin.media.model.MediaKind;

import static org.junit.jupiter.api.Assertions.*;

class MediaUtilsTest {

    @Test
    void detectKind_handlesContentTypes() {
        assertEquals(MediaKind.IMAGE, MediaUtils.detectKind("image/png"));
        assertEquals(MediaKind.VIDEO, MediaUtils.detectKind("video/mp4"));
        assertEquals(MediaKind.GIF, MediaUtils.detectKind("image/gif"));
        assertEquals(MediaKind.OTHER, MediaUtils.detectKind(null));
    }

    @Test
    void sanitizeOriginalFilename_stripsPathAndTruncates() {
        String sanitized = MediaUtils.sanitizeOriginalFilename("../path/to/file.png");

        assertEquals("file.png", sanitized);
        assertNull(MediaUtils.sanitizeOriginalFilename("   "));
    }

    @Test
    void pickExtension_prefersFilename() {
        assertEquals("png", MediaUtils.pickExtension("photo.PNG", "image/jpeg"));
        assertEquals("jpg", MediaUtils.pickExtension(null, "image/jpeg"));
        assertEquals("bin", MediaUtils.pickExtension(null, null));
    }

    @Test
    void buildStorageKeys_includeDate() {
        String tmp = MediaUtils.buildTmpStorageKey("png");
        String key = MediaUtils.buildStorageKey("png", "hash");

        assertTrue(tmp.startsWith("tmp/media/"));
        assertTrue(key.startsWith("media/"));
    }
}
