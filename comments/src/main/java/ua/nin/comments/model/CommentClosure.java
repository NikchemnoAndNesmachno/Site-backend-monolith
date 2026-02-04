package ua.nin.comments.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(schema = "comments", name = "comment_closure")
public class CommentClosure {

    @EmbeddedId
    private CommentClosureId id;

    @Column(name = "depth", nullable = false)
    private int depth;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @EqualsAndHashCode
    @Embeddable
    public static class CommentClosureId implements Serializable {

        @Column(name = "ancestor_id", nullable = false)
        private Long ancestorId;

        @Column(name = "descendant_id", nullable = false)
        private Long descendantId;
    }
}
