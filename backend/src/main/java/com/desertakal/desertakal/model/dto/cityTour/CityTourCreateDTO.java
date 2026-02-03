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
    @Min(value = 1, message = "Order index must be greater or equal to 0")
    private Integer orderIndex;

    @NotNull(message = "Days count for this city is required")
    @Min(value = 1, message = "Duration in each city must be at least 1 day")
    private Integer daysCount;

    @NotNull(message = "City UUID is required")
    private UUID cityUuid;
}
