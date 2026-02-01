package ua.nin.reactions.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ua.nin.reactions.model.ReactionCount;

import java.util.List;

public interface ReactionCountRepository extends JpaRepository<ReactionCount, ReactionCount.ReactionCountId> {

    @Query("""
            SELECT rc FROM ReactionCount rc
            WHERE rc.id.targetType = :targetType AND rc.id.targetId = :targetId
           """)
    List<ReactionCount> findByTarget(@Param("targetType") String targetType,
                                     @Param("targetId") long targetId);

    /**
     * delta: +1/-1
     * Для delta<0 і відсутнього рядка вставить 0 (не порушить checkConstraint).
     */
    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO reactions.reaction_counts (target_type, target_id, reaction_code, cnt, updated_at)
        VALUES (:targetType, :targetId, :reactionCode,
                CASE WHEN :delta > 0 THEN :delta ELSE 0 END,
                now())
        ON CONFLICT (target_type, target_id, reaction_code)
        DO UPDATE SET cnt = GREATEST(reactions.reaction_counts.cnt + :delta, 0),
                      updated_at = now()
        """, nativeQuery = true)
    void applyDelta(@Param("targetType") String targetType,
                    @Param("targetId") long targetId,
                    @Param("reactionCode") String reactionCode,
                    @Param("delta") long delta);
}
