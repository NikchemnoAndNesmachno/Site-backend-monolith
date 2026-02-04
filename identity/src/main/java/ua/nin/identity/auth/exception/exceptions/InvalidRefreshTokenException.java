package ua.nin.identity.auth.exception.exceptions;

public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException(String message) { super(message); }
}
