package ua.nin.identity.profile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.nin.identity.profile.model.Profile;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    boolean existsByUsername(String username);
    Optional<Profile> findByUserId(Long userId);

    boolean existsByUsernameAndUserIdNot(String username, Long userId);
    Optional<Profile> findByUsername(String username);
}
