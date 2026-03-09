package com.desertakal.desertakal.model.dto.reaction;

import com.desertakal.desertakal.model.enums.ReactionEnum;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactionCreateDTO {
    @NotNull(message = "Reaction is required")
    private ReactionEnum reaction;

    @NotNull(message = "Article UUID is required")
    private UUID articleUuid;
}
