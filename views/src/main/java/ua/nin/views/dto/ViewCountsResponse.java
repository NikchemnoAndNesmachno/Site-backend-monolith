package ua.nin.views.dto;

import java.time.Instant;

public record ViewCountsResponse(
        String targetType,
        long targetId,
        long totalViews,
        long uniqueViews,
        Instant updatedAt
) {}
