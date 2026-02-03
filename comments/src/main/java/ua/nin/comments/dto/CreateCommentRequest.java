package ua.nin.comments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
        @NotNull String targetType,
        @NotNull Long targetId,
        Long parentId,
        @NotBlank @Size(max = 500) String body
) {}
