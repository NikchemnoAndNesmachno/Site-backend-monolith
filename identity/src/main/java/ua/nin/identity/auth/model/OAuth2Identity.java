package ua.nin.identity.auth.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "oauth2_identities", schema = "authentication",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_oauth_user_id_provider", columnNames = {"user_id", "provider"}),
                @UniqueConstraint(name = "uq_oauth_provider_subject", columnNames = {"provider", "subject"})
        }
)
public class OAuth2Identity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "provider", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Column(name = "email", length = 64)
    private String email;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
