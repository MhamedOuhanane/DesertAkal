package com.desertakal.desertakal.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_token", columnList = "token"),
        @Index(name = "idx_family", columnList = "family_id"),
        @Index(name = "idx_user_family", columnList = "user_id, family_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String familyId;

    @Column(name = "parent_token", length = 500)
    private String parentToken;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiryAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "used_ed_at")
    private LocalDateTime usedAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(nullable = false)
    private boolean revoked = false;

    @Column(nullable = false)
    private boolean used = false;

    @Column(name = "reuse_detected", nullable = false)
    private boolean reuseDetected = false;

    private String ipAddress;
    private String userAgent;
    private String deviceId;

    @Version
    private Long version;

    @PrePersist
    public void prePersist() {
        if (expiryAt == null)
            expiryAt = LocalDateTime.now().plusDays(30);
    }
}
