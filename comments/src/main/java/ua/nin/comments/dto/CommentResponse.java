package ua.nin.comments.dto;

import ua.nin.comments.model.CommentStatus;

import java.time.Instant;

public record CommentResponse(
        long id,
        long authorUserId,
        String targetType,
        long targetId,
        Long parentId,
        String body,
        CommentStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
