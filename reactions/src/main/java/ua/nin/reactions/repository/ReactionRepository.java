package ua.nin.reactions.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import ua.nin.reactions.model.Reaction;
import ua.nin.reactions.repository.projection.MyVideoReactionRow;
import ua.nin.reactions.repository.projection.VideoReactionCountRow;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT r FROM Reaction r
            WHERE r.userId = :userId AND r.targetType = :targetType AND r.targetId = :targetId
           """)
    Optional<Reaction> findForUpdate(@Param("userId") long userId,
                                     @Param("targetType") String targetType,
                                     @Param("targetId") long targetId);

    @Query("""
            SELECT r FROM Reaction r
            WHERE r.userId = :userId AND r.targetType = :targetType AND r.targetId = :targetId
           """)
    Optional<Reaction> findAny(@Param("userId") long userId,
                               @Param("targetType") String targetType,
                               @Param("targetId") long targetId);

    @Query(value = """
            SELECT
                r.target_id AS videoId,
                r.cnt    AS cnt
            FROM reactions.reaction_counts r
            WHERE r.target_type = 'VIDEO'
              AND r.reaction_code = :reactionCode
              AND r.target_id IN (:videoIds)
            """,
            nativeQuery = true)
    List<VideoReactionCountRow> findReactionCountsByVideoIds(Collection<Long> videoIds, String reactionCode);

    @Query(value = """
            SELECT
                r.target_id      AS videoId,
                r.reaction_code  AS reactionCode
            FROM reactions.reactions r
            WHERE r.target_type = 'VIDEO'
              AND r.user_id = :userId
              AND r.revoked_at IS NULL
              AND r.target_id IN (:videoIds)
            """,
            nativeQuery = true)
    List<MyVideoReactionRow> findMyReactionsByVideoIds(long userId, Collection<Long> videoIds);
}
