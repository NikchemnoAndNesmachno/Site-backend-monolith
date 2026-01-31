package ua.nin.media.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.nin.media.model.MediaBundle;

@Repository
public interface MediaBundleRepository extends JpaRepository<MediaBundle, Long> {
}
