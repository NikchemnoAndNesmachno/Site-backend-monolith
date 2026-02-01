package ua.nin.reactions.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(
        schema = "reactions",
        name = "reactions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_reactions_user_target",
                        columnNames = {"user_id", "target_type", "target_id"}
                )
        },
        indexes = {
                @Index(name = "idx_reactions_user_id", columnList = "user_id"),
                @Index(name = "idx_reactions_target_type_id", columnList = "target_type,target_id"),
                @Index(name = "idx_reactions_target_reaction", columnList = "target_type,target_id,reaction_code"),
                @Index(name = "idx_reactions_revoked_at", columnList = "revoked_at")
        }
)
public class Reaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "target_type", nullable = false, length = 64)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "reaction_code", nullable = false, length = 32)
    private String reactionCode;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false)
    private Instant updatedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    public boolean isActive() {
        return revokedAt == null;
    }
}
