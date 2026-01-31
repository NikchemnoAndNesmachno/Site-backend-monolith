package ua.nin.media.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        schema = "media",
        name = "media_assets",
        indexes = {
                @Index(name = "idx_media_assets_created_at", columnList = "created_at"),
                @Index(name = "idx_media_assets_sha256", columnList = "sha256")
        }
)
public class MediaAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 32)
    private MediaKind kind;

    @Column(name = "content_type", nullable = false, length = 128)
    private String contentType;

    @Column(name = "original_filename", length = 255)
    private String originalFilename;

    /**
     * Storage key relative to storage root (e.g. 2026/01/23/uuid.mp4)
     */
    @Column(name = "storage_key", nullable = false, length = 512, unique = true)
    private String storageKey;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    /**
     * Hex SHA-256
     */
    @Column(name = "sha256", nullable = false, length = 64)
    private String sha256;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
