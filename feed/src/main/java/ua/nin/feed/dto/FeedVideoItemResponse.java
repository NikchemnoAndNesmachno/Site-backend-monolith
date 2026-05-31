package ua.nin.feed.dto;

import java.time.Instant;

public record FeedVideoItemResponse(
        Long videoId,
        String title,
        String description,
        Long previewMediaId,
        String previewUrl,
        FeedAuthorResponse author,
        long viewsCount,
        long likesCount,
        long dislikesCount,
        long commentsCount,
        String myReaction,
        Instant createdAt
) {}
