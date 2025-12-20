package com.desertakal.desertakal.model.dto.cityTour;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityTourCreateDTO {
    @NotNull(message = "Order index is required")
    @Min(value = 0, message = "Order index must be greater or equal to 0")
    private Integer orderIndex;

    @NotNull(message = "City UUID is required")
    private UUID cityUuid;

    @NotNull(message = "Tour UUID is required")
    private UUID tourUuid;
}
