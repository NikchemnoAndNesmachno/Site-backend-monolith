package ua.nin.comments.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ua.nin.comments.model.Comment;
import ua.nin.comments.repository.projection.VideoCommentCountRow;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByTargetTypeAndTargetIdAndParentIdIsNullOrderByCreatedAtDesc(
            String targetType, Long targetId, Pageable pageable
    );

    Page<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId, Pageable pageable);

    Optional<Comment> findByIdAndTargetTypeAndTargetId(Long id, String targetType, Long targetId);

    @Query(value = """
            SELECT
                c.target_id AS videoId,
                COUNT(*)    AS cnt
            FROM comments.comments c
            WHERE c.target_type = 'VIDEO'
              AND c.status = 'ACTIVE'
              AND c.target_id IN (:videoIds)
            GROUP BY c.target_id
            """,
            nativeQuery = true)
    List<VideoCommentCountRow> findCommentCountsByVideoIds(Collection<Long> videoIds);
}
