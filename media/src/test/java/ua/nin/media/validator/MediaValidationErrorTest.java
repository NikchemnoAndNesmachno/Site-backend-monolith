package ua.nin.media.validator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class MediaValidationErrorTest {

    @Test
    void enumValues_present() {
        for (MediaValidationError error : MediaValidationError.values()) {
            assertNotNull(error.name());
        }
    }
}
