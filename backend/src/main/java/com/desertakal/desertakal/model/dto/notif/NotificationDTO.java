package com.desertakal.desertakal.model.dto.notif;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private UUID uuid;
    private String title;
    private String message;
    private Boolean seen;
    private LocalDateTime date;
    private Long userId;
    private String userName;
    private String userPhoto;
}
