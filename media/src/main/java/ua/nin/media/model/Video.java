package ua.nin.media.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        schema = "media",
        name = "videos",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_videos_media_bundle", columnNames = "media_bundle_id")
        },
        indexes = {
                @Index(name = "idx_videos_owner", columnList = "owner_user_id"),
                @Index(name = "idx_videos_created_at", columnList = "created_at"),
                @Index(name = "idx_videos_visibility_status", columnList = "visibility,status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_user_id")
    private Long ownerUserId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 32)
    private VideoVisibility visibility;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private VideoStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * 1:1 связь с media_bundles (уникальность на уровне БД).
     * Это твой "контейнер файлов": видео, превью, варианты качества и т.д.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JoinColumn(
            name = "media_bundle_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_videos_bundle")
    )
    @ToString.Exclude
    private MediaBundle mediaBundle;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}

