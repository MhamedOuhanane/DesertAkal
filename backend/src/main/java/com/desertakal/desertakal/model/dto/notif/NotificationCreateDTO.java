package com.desertakal.desertakal.model.dto.notif;

import com.desertakal.desertakal.model.entity.User;
import com.desertakal.desertakal.model.enums.OauthProvider;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCreateDTO {
    private String title;
    private String message;
    private Long userId;
}
