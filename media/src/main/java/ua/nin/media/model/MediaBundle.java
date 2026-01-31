package ua.nin.media.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        schema = "media",
        name = "media_bundles",
        indexes = {
                @Index(name = "idx_media_bundles_owner_user_id", columnList = "owner_user_id")
        })
public class MediaBundle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "owner_user_id")
    private Long ownerUserId;

    @Column(name = "type", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private BundleType type;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "bundle", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<MediaBundleItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.type == null) {
            this.type = BundleType.GENERIC;
        }
    }
}
