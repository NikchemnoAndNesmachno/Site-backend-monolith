package ua.nin.identity.auth.exception.exceptions;

public class TokenHashingException extends RuntimeException {
    public TokenHashingException(String message, Throwable cause) {
        super(message, cause);
    }
}