package ua.nin.identity.auth.service;

public interface EmailSenderService {
    void sendEmailVerification(String email, String rawToken);
    void sendForgotPassword(String email, String rawToken);
}