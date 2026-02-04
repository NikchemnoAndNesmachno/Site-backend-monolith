package ua.nin.identity.auth.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "credentials", schema = "authentication")
public class Credential implements Serializable {
    @Id
    @Column(name="user_id")
    private Long userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "password_updated_at")
    private Instant passwordUpdatedAt;

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts;

    @Column(name = "lock_until")
    private Instant lockUntil;

    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
    }
}
