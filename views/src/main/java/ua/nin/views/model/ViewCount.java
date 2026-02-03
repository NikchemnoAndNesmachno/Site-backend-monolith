package ua.nin.views.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(schema = "views", name = "view_counts")
public class ViewCount {

    @EmbeddedId
    private ViewCountId id;

    @Column(name = "total_views", nullable = false)
    private long totalViews;

    @Column(name = "unique_views", nullable = false)
    private long uniqueViews;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @EqualsAndHashCode
    public static class ViewCountId implements Serializable {
        @Column(name = "target_type", length = 64)
        private String targetType;
        @Column(name = "target_id")
        private Long targetId;
    }

}
