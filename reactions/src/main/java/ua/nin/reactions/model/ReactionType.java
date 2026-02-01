package ua.nin.reactions.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(schema = "reactions", name = "reaction_types")
public class ReactionType {

    @Id
    @Column(name = "code", length = 32)
    private String code;

    @Column(name = "weight", nullable = false)
    private short weight;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private Instant createdAt;
}
