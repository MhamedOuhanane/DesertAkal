package com.desertakal.desertakal.model.dto.city;

import lombok.*;

import java.math.BigDecimal;

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
    private String coverFileName;
}
