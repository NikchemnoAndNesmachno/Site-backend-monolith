package ua.nin.identity.auth.exception.exceptions;

public class TokenAlreadyUsedException extends RuntimeException {
    public TokenAlreadyUsedException(String message) {
        super(message);
    }
}
