package com.desertakal.desertakal.model.dto.city;

import com.desertakal.desertakal.model.dto.image.ImageDTO;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityFIndDTO {
    private UUID uuid;
    private String name;
    private BigDecimal map_lat;
    private BigDecimal map_lng;
    private String description;
    private List<ImageDTO> images;
}
