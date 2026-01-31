package ua.nin.media.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import ua.nin.media.model.VideoVisibility;

@Builder
public record VideoWithPreviewUploadRequest(@NotNull String title, String description, VideoVisibility visibility) {
}
