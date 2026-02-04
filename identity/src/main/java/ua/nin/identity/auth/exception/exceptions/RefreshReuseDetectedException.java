package ua.nin.identity.auth.exception.exceptions;

public class RefreshReuseDetectedException extends RuntimeException {
  public RefreshReuseDetectedException(String message) { super(message); }
}
