package com.desertakal.desertakal.model.dto.city;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityCreateDTO {
    @NotBlank(message = "City name is required")
    @Size(min = 4, max = 100, message = "City name must be between 4 and 100 characters")
    private String name;

    @NotNull(message = "Latitude is required")
    private BigDecimal map_lat;

    @NotNull(message = "Longitude is required")
    private BigDecimal map_lng;

    @NotNull(message = "Description is required")
    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    @Size(max = 255, message = "Cover file name must be at most 255 characters")
    private String coverFileName;
}
