package ua.nin.media.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ua.nin.contract.feed.dto.FeedVideoBaseView;
import ua.nin.media.model.Video;
import ua.nin.media.repository.projection.VideoFeedRowProjection;

import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Long> {
    Optional<Video> findByMediaBundleId(Long mediaBundleId);

    @Query(value = """
            SELECT
                v.id                    AS videoId,
                v.title                 AS title,
                v.description           AS description,
                v.owner_user_id         AS ownerUserId,
                p.username              AS ownerUsername,
                p.display_name          AS ownerDisplayName,
                a.media_asset_id         AS ownerAvatarMediaId,
                mbi.media_id            AS previewMediaId,
                v.created_at            AS createdAt
            FROM media.videos v
            JOIN profile.profiles p
              ON p.user_id = v.owner_user_id
            LEFT JOIN media.user_avatars a
              ON a.owner_user_id = v.owner_user_id
            LEFT JOIN media.media_bundle_items mbi
              ON mbi.bundle_id = v.media_bundle_id
             AND mbi.role = 'PREVIEW'
            WHERE v.visibility = 'PUBLIC'
              AND v.status = 'READY'
            ORDER BY v.created_at DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM media.videos v
            WHERE v.visibility = 'PUBLIC'
              AND v.status = 'READY'
            """,
            nativeQuery = true)
    Page<VideoFeedRowProjection> findPublicFeedLatest(Pageable pageable);

    @Query(value = """
            SELECT
                v.id                    AS videoId,
                v.title                 AS title,
                v.description           AS description,
                v.owner_user_id         AS ownerUserId,
                p.username              AS ownerUsername,
                p.display_name          AS ownerDisplayName,
                a.media_asset_id         AS ownerAvatarMediaId,
                mbi.media_id            AS previewMediaId,
                v.created_at            AS createdAt
            FROM media.videos v
            JOIN profile.profiles p
              ON p.user_id = v.owner_user_id
            LEFT JOIN media.user_avatars a
              ON a.owner_user_id = v.owner_user_id
            LEFT JOIN media.media_bundle_items mbi
              ON mbi.bundle_id = v.media_bundle_id
             AND mbi.role = 'PREVIEW'
            LEFT JOIN views.view_counts vc
              ON vc.target_type = 'VIDEO'
             AND vc.target_id = v.id
            WHERE v.visibility = 'PUBLIC'
              AND v.status = 'READY'
//          -- ORDER BY COALESCE(vc.total_views, 0) DESC, v.created_at DESC
            ORDER BY vc.total_views DESC NULLS LAST
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM media.videos v
            WHERE v.visibility = 'PUBLIC'
              AND v.status = 'READY'
            """,
            nativeQuery = true)
    Page<VideoFeedRowProjection> findPublicFeedPopular(Pageable pageable);
}