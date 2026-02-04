package ua.nin.media.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MediaPropertiesTest {

    @Test
    void defaults_areSet() {
        MediaProperties properties = new MediaProperties();

        assertNotNull(properties.getStorage().getLocal().getRoot());
        assertTrue(properties.getLimits().getMaxSizeBytes() > 0);
    }
}
