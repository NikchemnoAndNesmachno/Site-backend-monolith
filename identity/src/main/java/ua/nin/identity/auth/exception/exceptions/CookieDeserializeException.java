package ua.nin.identity.auth.exception.exceptions;

public class CookieDeserializeException extends RuntimeException {
    public CookieDeserializeException(String message, Throwable cause) {
        super(message, cause);
    }

    public CookieDeserializeException(String message) {
        super(message);
    }
}
