package com.desertakal.desertakal.model.dto.RefreshToken;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenFullDTO {
    private UUID uuid;
    private String token;
    private UUID userUuid;
    private String userName;
    private String familyId;
    private String parentToken;
    private LocalDateTime createdAt;
    private LocalDateTime expiryAt;
    private LocalDateTime usedAt;
    private LocalDateTime revokedAt;
    private boolean revoked;
    private boolean used;
    private boolean reuseDetected;
    private String ipAddress;
    private String userAgent;
    private String deviceId;
}

