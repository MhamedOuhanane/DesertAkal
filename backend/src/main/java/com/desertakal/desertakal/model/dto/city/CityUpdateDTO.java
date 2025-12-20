package com.desertakal.desertakal.model.dto.city;

import com.desertakal.desertakal.model.dto.image.ImageDTO;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityUpdateDTO {
    @Size(min = 4, max = 100, message = "City name must be between 4 and 100 characters")
    private String name;

    private BigDecimal map_lat;
    private BigDecimal map_lng;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;
}
