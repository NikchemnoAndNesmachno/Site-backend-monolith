package ua.nin.identity.auth.model;

import jakarta.persistence.*;
import lombok.*;

import java.net.InetAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        schema = "authentication",
        name = "refresh_token_families",
        indexes = {
                @Index(name = "idx_refresh_token_families_user_id", columnList = "user_id"),
                @Index(name = "idx_refresh_token_families_revoked_at", columnList = "revoked_at")
        }
)
public class RefreshTokenFamily {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    /**
     * Postgres inet
     */
    @Column(name = "ip", columnDefinition = "inet")
    private InetAddress ip;

    @OneToMany(mappedBy = "family", fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<RefreshToken> tokens = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }
}
