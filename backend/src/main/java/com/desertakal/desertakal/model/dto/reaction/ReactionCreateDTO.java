package com.desertakal.desertakal.model.dto.reaction;

import com.desertakal.desertakal.model.enums.ReactionEnum;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactionCreateDTO {
    private ReactionEnum reaction;
}
