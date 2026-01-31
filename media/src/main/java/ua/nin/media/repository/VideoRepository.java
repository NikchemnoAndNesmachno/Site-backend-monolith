package ua.nin.media.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.nin.media.model.Video;

import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Long> {
    Optional<Video> findByMediaBundleId(Long mediaBundleId);
}