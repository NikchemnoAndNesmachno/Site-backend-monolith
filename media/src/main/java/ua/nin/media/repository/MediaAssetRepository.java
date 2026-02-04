package ua.nin.media.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.nin.media.model.MediaAsset;

import java.util.Optional;

@Repository
public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {

    Optional<MediaAsset> findByIdAndDeletedAtIsNull(Long id);


    Optional<MediaAsset> findBySha256AndSizeBytesAndDeletedAtIsNull(String sha256, Long sizeBytes);
    boolean existsBySha256AndSizeBytesAndDeletedAtIsNull (String sha256, Long sizeBytes);
}
