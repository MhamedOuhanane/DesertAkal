package com.desertakal.desertakal.model.dto.image;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ImageCreateDTO {
    private String image;
    private Boolean isCover;
    private UUID cityUuid;
}
