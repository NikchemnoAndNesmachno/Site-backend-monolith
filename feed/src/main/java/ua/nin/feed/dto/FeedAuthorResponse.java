package ua.nin.feed.dto;

public record FeedAuthorResponse(
        Long userId,
        String username,
        String displayName,
        Long avatarMediaId,
        String avatarUrl
) {}