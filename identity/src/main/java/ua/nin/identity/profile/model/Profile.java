package ua.nin.identity.profile.model;

import jakarta.persistence.*;
import lombok.*;
import ua.nin.identity.auth.model.User;

import java.time.LocalDateTime;

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

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

    @Column(name = "username", nullable = false, length = 64)
    private String username;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "avatar_media_id")
    private Long avatarMediaId;

    @Column(name = "bio")
    private String bio;

    @Column(name = "privacy")
    @Enumerated(EnumType.STRING)
    private Privacy privacy;

    @Column(name = "locale")
    private String locale;

    @Column(name = "timezone", length = 64)
    private String timezone;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
