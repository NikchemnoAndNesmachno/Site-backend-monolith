package ua.nin.reactions.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.nin.reactions.model.ReactionType;

public interface ReactionTypeRepository extends JpaRepository<ReactionType, String> {
}
