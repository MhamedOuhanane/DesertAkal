package com.desertakal.desertakal.model.dto.refreshToken;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveSessionDTO {
    private UUID sessionUuid;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime lastActive;
    private LocalDateTime expiresAt;
}

