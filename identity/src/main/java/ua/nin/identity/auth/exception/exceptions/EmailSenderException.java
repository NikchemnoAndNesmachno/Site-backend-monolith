package ua.nin.identity.auth.exception.exceptions;

public class EmailSenderException extends RuntimeException {
    public EmailSenderException(String message, Throwable cause) {
        super(message, cause);
    }
}
