package ua.nin.reactions.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record PutReactionRequest(
        @NotBlank @Size(max = 64)
        // твій формат: POSTS, COMMENTS, VIDEOS і т.д. - Вибери один стиль і тримай його
        @Pattern(regexp = "^[A-Z0-9_]+$", message = "targetType must be UPPER_SNAKE_CASE")
        String targetType,

        @NotNull
        Long targetId,

        @NotBlank @Size(max = 32)
        @Pattern(regexp = "^[A-Z0-9_]+$", message = "reactionCode must be UPPER_SNAKE_CASE")
        String reactionCode
) {}
