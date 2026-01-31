package ua.nin.media.exception.exceptions;

import lombok.Getter;
import ua.nin.media.validator.MediaValidationError;

@Getter
public class MediaValidationException extends RuntimeException {
    private final MediaValidationError error;

    public MediaValidationException(MediaValidationError error, String message) {
        super(message);
        this.error = error;
    }
}