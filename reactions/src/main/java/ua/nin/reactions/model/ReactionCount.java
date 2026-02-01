package ua.nin.reactions.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(schema = "reactions", name = "reaction_counts")
public class ReactionCount {

    @EmbeddedId
    private ReactionCountId id;

    @Column(name = "cnt", nullable = false)
    private long count;

    @Column(name = "updated_at", nullable = false, insertable = false)
    private Instant updatedAt;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Embeddable
    public static class ReactionCountId implements Serializable {
        @Column(name = "target_type", length = 32)
        private String targetType;

        @Column(name = "target_id")
        private Long targetId;

        @Column(name = "reaction_code", length = 32)
        private String reactionCode;
    }
}
