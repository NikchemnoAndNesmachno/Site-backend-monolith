package ua.nin.reactions.dto;

import java.time.Instant;
import java.util.Map;

public record ReactionActionResponse(
        String targetType,
        long targetId,
        String myReaction,          // null якщо реакції нема
        Map<String, Long> counts,   // LIKE -> 10, DISLIKE -> 2
        Instant updatedAt
) {}
