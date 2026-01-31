package ua.nin.media.dto;

import lombok.Builder;

@Builder
public record AvatarUploadResponse(
        long avatarId,
        long avatarAssetId
) {
}
