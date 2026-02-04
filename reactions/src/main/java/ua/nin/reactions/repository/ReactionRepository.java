package ua.nin.reactions.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import ua.nin.reactions.model.Reaction;

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
}
