package ua.nin.identity.auth.dto;

public record AuthResult(
        AuthResponse authResponse,
        String refreshToken
) {}