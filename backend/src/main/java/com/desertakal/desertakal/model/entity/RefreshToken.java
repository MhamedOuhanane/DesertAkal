package com.desertakal.desertakal.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_token", columnList = "token"),
        @Index(name = "idx_family", columnList = "family_id"),
        @Index(name = "idx_user_device", columnList = "user_id, device_id"),
        @Index(name = "idx_family_active", columnList = "family_id, revoked, used"),
        @Index(name = "idx_user_family", columnList = "user_id, family_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RefreshToken implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @EqualsAndHashCode.Include
    @Column(columnDefinition = "uuid", updatable = false, nullable = false, unique = true)
    private UUID uuid;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "family_id", nullable = false, length = 100)
    private UUID familyId;

    @Column(name = "parent_token", length = 500)
    private String parentToken;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Builder.Default
    @Column(nullable = false)
    private boolean revoked = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean used = false;

    @Builder.Default
    @Column(name = "reuse_detected", nullable = false)
    private boolean reuseDetected = false;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "device_id")
    private String deviceId;

    @Version
    private Long version;

    @Serial
    private static final long serialVersionUID = 1L;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null)
            this.uuid = UUID.randomUUID();

        if (this.familyId == null)
            this.familyId = UUID.randomUUID();

        if (expiresAt == null)
            expiresAt = LocalDateTime.now().plusDays(30);
    }
}
