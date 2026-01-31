package ua.nin.media.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.nin.media.model.UserAvatar;

public interface UserAvatarRepository extends JpaRepository<UserAvatar, Long> {
}
