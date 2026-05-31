package ua.nin.identity.auth.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        long userId,
        String email,
        String role
) {}