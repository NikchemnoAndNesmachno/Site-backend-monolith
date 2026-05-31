package ua.nin.contract.feed.dto;

import java.time.Instant;

public record FeedVideoBaseView(
        Long videoId,
        String title,
        String description,
        Long ownerUserId,
        String ownerUsername,
        String ownerDisplayName,
        Long ownerAvatarMediaId,
        Long previewMediaId,
        Instant createdAt
) {}