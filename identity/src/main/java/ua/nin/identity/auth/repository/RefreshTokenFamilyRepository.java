package ua.nin.identity.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ua.nin.identity.auth.model.RefreshTokenFamily;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenFamilyRepository extends JpaRepository<RefreshTokenFamily, Long> {

    Optional<RefreshTokenFamily> findByIdAndUser_Id(Long id, Long userId);

    @Modifying
    @Query("""
        UPDATE RefreshTokenFamily f
           SET f.revokedAt = :now
         WHERE f.id = :familyId
           AND f.revokedAt is null
        """)
    void revokeFamily(long familyId, Instant now);

    @Modifying
    @Query("""
        UPDATE RefreshTokenFamily f
           SET f.lastUsedAt = :now,
               f.userAgent = :userAgent,
               f.ip = :ip
         WHERE f.id = :familyId
        """)
    void touch(long familyId, Instant now, String userAgent, InetAddress ip);

    @Modifying
    @Query("""
        UPDATE RefreshTokenFamily f
           SET f.revokedAt = :now
         WHERE f.user.id = :userId
           AND f.revokedAt is null
        """)
    void revokeAllForUser(long userId, Instant now);
}
