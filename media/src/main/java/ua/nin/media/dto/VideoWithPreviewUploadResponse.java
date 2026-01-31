package ua.nin.media.dto;

import lombok.Builder;

@Builder
public record VideoWithPreviewUploadResponse(
        long videoId,
        long bundleId,
        long videoAssetId,
        long previewAssetId
) {
}
