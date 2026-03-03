package ua.nin.identity.auth.exception.exceptions;

public class InvalidJwtSecretException extends RuntimeException {
    public InvalidJwtSecretException(String message) {
        super(message);
    }
}
