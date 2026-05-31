package ua.nin.media.dto;

public record VideoDetailsAuthorResponse(
        Long userId,
        String username,
        String displayName,
        Long avatarMediaId,
        String avatarUrl
) {}