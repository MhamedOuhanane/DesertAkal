package com.desertakal.desertakal.model.dto.comment;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private UUID uuid;
    private String content;
    private LocalDateTime createdAt;
    private UUID articleUuid;
    private UUID userUuid;
    private String userName;
    private String userPhoto;
}
