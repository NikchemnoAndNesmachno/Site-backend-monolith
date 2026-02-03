package ua.nin.views.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import ua.nin.views.model.ViewCount;

import static ua.nin.views.model.ViewCount.ViewCountId;

public interface ViewCountRepository extends Repository<ViewCount, ViewCountId> {

    @Modifying
    @Query(value = """
        INSERT INTO views.view_counts (target_type, target_id, total_views, unique_views, updated_at)
        VALUES (:targetType, :targetId,
                        CASE WHEN :totalInc > 0 THEN :totalInc ELSE 0 END,
                        CASE WHEN :uniqueInc > 0 THEN :uniqueInc ELSE 0 END,
                        now())
        ON CONFLICT (target_type, target_id)
        DO UPDATE SET
            total_views = GREATEST(views.view_counts.total_views + :totalInc, 0),
            unique_views = GREATEST(views.view_counts.unique_views + :uniqueInc, 0),
            updated_at = now()
        """, nativeQuery = true)
    void upsertIncrement(
            @Param("targetType") String targetType,
            @Param("targetId") long targetId,
            @Param("totalInc") long totalInc,
            @Param("uniqueInc") long uniqueInc
    );

    @Query("""
            SELECT vc FROM ViewCount vc
            WHERE vc.id.targetType = :targetType AND vc.id.targetId = :targetId
           """)
    ViewCount findCountsByTarget(@Param("targetType") String targetType,
                                       @Param("targetId") long targetId);
}
