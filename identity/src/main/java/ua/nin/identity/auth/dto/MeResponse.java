package ua.nin.identity.auth.dto;

public record MeResponse(
        long userId,
        String email,
        String status,
        String role
) {}