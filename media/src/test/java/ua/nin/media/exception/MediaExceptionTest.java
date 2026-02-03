package ua.nin.media.exception;

import org.junit.jupiter.api.Test;
import ua.nin.media.exception.exceptions.MediaValidationException;
import ua.nin.media.validator.MediaValidationError;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MediaExceptionTest {

    @Test
    void exception_holdsFields() {
        MediaValidationException ex = new MediaValidationException(MediaValidationError.EMPTY_FILE, "Empty file");

        assertEquals(MediaValidationError.EMPTY_FILE, ex.getError());
        assertEquals("Empty file", ex.getMessage());
    }
}
