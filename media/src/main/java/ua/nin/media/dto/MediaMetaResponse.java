package ua.nin.media.dto;

import ua.nin.media.model.MediaKind;

import java.time.Instant;

public record MediaMetaResponse(
        long id,
        MediaKind kind,
        String contentType,
        String originalFilename,
        long sizeBytes,
        String sha256,
        Instant createdAt,
        Instant deletedAt
) {
}
