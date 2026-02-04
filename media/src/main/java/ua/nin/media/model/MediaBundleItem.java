package ua.nin.media.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@IdClass(MediaBundleItemId.class)
@Table(
        schema = "media",
        name = "media_bundle_items",
//        uniqueConstraints = {
//                @UniqueConstraint(name = "uq_media_bundle_items_media_id", columnNames = "media_id")
//        },
        indexes = {
                @Index(name = "idx_media_bundle_items_bundle", columnList = "bundle_id"),
                @Index(name = "idx_media_bundle_items_media", columnList = "media_id")
        }
)
public class MediaBundleItem {

    @Id
    @Column(name = "bundle_id", nullable = false)
    private Long bundleId;

    @Id
    @Column(name = "role", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private BundleItemRole role;

    @Column(name = "media_id", nullable = false)
    private Long mediaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bundle_id", insertable = false, updatable = false)
    @ToString.Exclude
    private MediaBundle bundle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id", insertable = false, updatable = false)
    @ToString.Exclude
    private MediaAsset asset;

    @PrePersist
    protected void onCreate() {
        if (this.role == null) {
            this.role = BundleItemRole.GENERIC;
        }
    }
}
