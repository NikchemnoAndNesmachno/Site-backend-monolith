package ua.nin.views.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface ViewUniqueRepository extends Repository<Object, Long> {

    @Modifying
    @Query(value = """
        INSERT INTO views.view_uniques (target_type, target_id, viewer_key_hash, bucket_start, created_at)
        VALUES (:targetType, :targetId, :viewerHash, :bucketStart, :now)
        ON CONFLICT DO NOTHING
        """, nativeQuery = true)
    int insertUniqueIfAbsent(
            @Param("targetType") String targetType,
            @Param("targetId") long targetId,
            @Param("viewerHash") String viewerHash,
            @Param("bucketStart") Instant bucketStart,
            @Param("now") Instant now
    );
}
