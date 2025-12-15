package com.desertakal.desertakal.model.dto.reaction;

import com.desertakal.desertakal.model.enums.ReactionEnum;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReacionDTO {
    private UUID uuid;
    private ReactionEnum reaction;
    private String emoji;
    private LocalDateTime createdAt;
    private UUID articleUuid;
    private UUID userUuid;
    private String userName;
    private String userPhoto;
}
