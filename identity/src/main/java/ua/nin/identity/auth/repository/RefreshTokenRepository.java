package ua.nin.identity.auth.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ua.nin.identity.auth.model.RefreshToken;
import ua.nin.identity.auth.model.RefreshTokenFamily;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select rt
        from RefreshToken rt
        join fetch rt.family
        where rt.tokenHash = :tokenHash
    """)
    Optional<RefreshToken> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    @Modifying
    @Query("""
        UPDATE RefreshToken t
           SET t.revokedAt = :now
         WHERE t.family = :family
           AND t.revokedAt is null
        """)
    void revokeAllInFamily(RefreshTokenFamily family, Instant now);

    @Modifying
    @Query("""
        UPDATE RefreshToken t
           SET t.revokedAt = :now
         WHERE t.user.id = :userId
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
