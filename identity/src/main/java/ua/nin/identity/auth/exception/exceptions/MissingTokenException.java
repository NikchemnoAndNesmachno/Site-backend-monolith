package ua.nin.identity.auth.exception.exceptions;

public class MissingTokenException extends RuntimeException {
    public MissingTokenException(String message) {
        super(message);
    }
}
