package com.desertakal.desertakal.model.dto.city;

import com.desertakal.desertakal.model.dto.image.ImageCreateDTO;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityCreateDTO {
    private String name;
    private BigDecimal map_lat;
    private BigDecimal map_lng;
    private String description;
    private List<ImageCreateDTO> images;

}
