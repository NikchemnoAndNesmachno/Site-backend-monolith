package ua.nin.media.repository.projection;

import java.time.Instant;

public interface VideoFeedRowProjection {
    Long getVideoId();
    String getTitle();
    String getDescription();

    Long getOwnerUserId();
    String getOwnerUsername();
    String getOwnerDisplayName();
    Long getOwnerAvatarMediaId();

    Long getPreviewMediaId();
    Instant getCreatedAt();
}
