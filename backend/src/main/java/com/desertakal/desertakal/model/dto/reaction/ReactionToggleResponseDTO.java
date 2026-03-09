package com.desertakal.desertakal.model.dto.reaction;

import com.desertakal.desertakal.model.enums.ReactionEnum;
import lombok.*;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactionToggleResponseDTO {
    private String action;

    private ReactionEnum userReaction;
    private long totalCount;
    private Map<ReactionEnum, Long> countByType;
    private UUID articleUuid;
}