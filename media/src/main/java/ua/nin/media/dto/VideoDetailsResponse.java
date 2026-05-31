package ua.nin.media.dto;

import java.time.Instant;

public record VideoDetailsResponse(
        Long videoId,
        String title,
        String description,
        Long videoMediaId,
        String videoUrl,
        Long previewMediaId,
        String previewUrl,
        VideoDetailsAuthorResponse author,
        Instant createdAt
) {}
