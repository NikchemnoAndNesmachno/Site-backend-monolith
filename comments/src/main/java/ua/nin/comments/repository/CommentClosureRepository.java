package ua.nin.comments.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import ua.nin.comments.model.CommentClosure;

import static ua.nin.comments.model.CommentClosure.CommentClosureId;

public interface CommentClosureRepository extends Repository<CommentClosure, CommentClosureId> {

    @Modifying
    @Query(value = """
        INSERT INTO comments.comment_closure (ancestor_id, descendant_id, depth)
        VALUES (:id, :id, 0)
        """, nativeQuery = true)
    void insertSelf(@Param("id") long id);

    @Modifying
    @Query(value = """
        INSERT INTO comments.comment_closure (ancestor_id, descendant_id, depth)
        SELECT cc.ancestor_id, :newId, cc.depth + 1
        FROM comments.comment_closure cc
        WHERE cc.descendant_id = :parentId
        UNION ALL
        SELECT :newId, :newId, 0
        """, nativeQuery = true)
    void insertForReply(@Param("parentId") long parentId, @Param("newId") long newId);

    @Query(value = """
        SELECT COALESCE(MAX(depth), 0)
        FROM comments.comment_closure
        WHERE descendant_id = :commentId
        """, nativeQuery = true)
    int maxDepthFromAncestors(@Param("commentId") long commentId);
}
