package ua.nin.media.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.nin.media.model.MediaBundleItem;
import ua.nin.media.model.MediaBundleItemId;

import java.util.List;

@Repository
public interface MediaBundleItemRepository extends JpaRepository<MediaBundleItem, MediaBundleItemId> {
    List<MediaBundleItem> findAllByBundleId(Long bundleId);
}
