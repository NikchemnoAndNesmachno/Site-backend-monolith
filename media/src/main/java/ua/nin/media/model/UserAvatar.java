package ua.nin.media.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "user_avatars",
        schema = "media",
        indexes = {
                @Index(name = "idx_user_avatars_asset", columnList = "media_asset_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserAvatar {

    /**
     * PK = owner_user_id (как в миграции).
     * User как Entity не маппим (другой schema/модуль), храним просто id.
     */
    @Id
    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    /**
     * FK на media_assets.
     * Asset не удаляется каскадом.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "media_asset_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_avatars_asset")
    )
    @ToString.Exclude
    private MediaAsset mediaAsset;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
