package com.desertakal.desertakal.model.dto.city;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityDTO {
    private UUID uuid;
    private String name;
    private BigDecimal map_lat;
    private BigDecimal map_lng;
}
