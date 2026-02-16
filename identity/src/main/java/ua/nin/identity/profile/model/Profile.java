package ua.nin.identity.profile.model;

import jakarta.persistence.*;
import lombok.*;
import ua.nin.identity.auth.model.User;

import java.time.Instant;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "profiles", schema = "profile")
public class Profile {
    @Id
    @Column(name="user_id")
    private Long userId;

    @Column(name = "username", nullable = false, length = 64)
    private String username;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "bio")
    private String bio;

    @Column(name="privacy", nullable=false)
    @Enumerated(EnumType.STRING)
    private Privacy privacy;

    @Column(name = "locale")
    private String locale;

    @Column(name = "timezone", length = 64)
    private String timezone;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
