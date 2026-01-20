package ua.nin.identity.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ua.nin.identity.auth.model.RefreshToken;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
        UPDATE RefreshToken t
           SET t.revokedAt = :now
         WHERE t.familyId = :familyId
           AND t.revokedAt is null
        """)
    void revokeAllInFamily(long familyId, Instant now);

    @Modifying
    @Query("""
        UPDATE RefreshToken t
           SET t.revokedAt = :now
         WHERE t.user.id = :usedId
           AND t.revokedAt is null
        """)
    void revokeAllForUser(long userId, Instant now);

    @Modifying
    @Query("""
        DELETE FROM RefreshToken t
         WHERE t.expiresAt < :now
        """)
    void deleteExpired(Instant now);
}
