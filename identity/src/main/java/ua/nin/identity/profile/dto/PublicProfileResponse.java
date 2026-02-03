package ua.nin.identity.profile.dto;

public record PublicProfileResponse(
        String username,
        String displayName,
        String bio
) {}