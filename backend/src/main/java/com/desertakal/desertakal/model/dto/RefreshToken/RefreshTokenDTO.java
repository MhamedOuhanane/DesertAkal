package com.desertakal.desertakal.model.dto.RefreshToken;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenDTO {
    private UUID uuid;
    private String token;
    private UUID userUuid;
    private String familyId;
    private String deviceId;
    private String userAgent;
    private String ipAddress;
    private LocalDateTime createdAt;
    private LocalDateTime expiryAt;
    private boolean revoked;
    private boolean used;
}

