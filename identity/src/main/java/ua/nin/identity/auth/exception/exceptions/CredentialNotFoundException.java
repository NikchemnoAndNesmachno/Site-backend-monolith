package ua.nin.identity.auth.exception.exceptions;

public class CredentialNotFoundException extends RuntimeException {
    public CredentialNotFoundException(String message) {
        super(message);
    }
}
