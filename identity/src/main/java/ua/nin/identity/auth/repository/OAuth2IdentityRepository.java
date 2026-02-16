package ua.nin.identity.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.nin.identity.auth.model.OAuth2Identity;
import ua.nin.identity.auth.model.Provider;

import java.util.Optional;

public interface OAuth2IdentityRepository extends JpaRepository<OAuth2Identity, Long> {
    Optional<OAuth2Identity> findByProviderAndSubject(Provider provider, String subject);
    boolean existsByUserIdAndProvider(Long userId, Provider provider);
}