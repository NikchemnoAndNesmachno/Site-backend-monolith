package ua.nin.comments.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.nin.comments.model.Comment;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByTargetTypeAndTargetIdAndParentIdIsNullOrderByCreatedAtDesc(
            String targetType, Long targetId, Pageable pageable
    );

    Page<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId, Pageable pageable);

    Optional<Comment> findByIdAndTargetTypeAndTargetId(Long id, String targetType, Long targetId);
}
