package ua.nin.views.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(schema = "views", name = "view_uniques")
public class ViewUnique {

    @EmbeddedId
    private ViewUniqueId id;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
    }

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @EqualsAndHashCode
    public static class ViewUniqueId implements Serializable {

        @Column(name = "target_type", length = 64)
        private String targetType;

        @Column(name = "target_id")
        private Long targetId;

        @Column(name = "viewer_key_hash", length = 64)
        private String viewerKeyHash;

        @Column(name = "bucket_start")
        private Instant bucketStart;
    }
}
