package ua.nin.identity.auth.service;

public interface EmailSenderService {
    void sendEmailVerification(String to, String rawToken);
}