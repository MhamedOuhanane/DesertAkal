package com.desertakal.desertakal.model.dto.refreshToken;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequestDTO {
    private UUID userUuid;
    private String deviceId;
    private String userAgent;
    private String ipAddress;
}

