package ua.nin.identity.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.nin.identity.auth.model.Credential;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, Long> {
}
