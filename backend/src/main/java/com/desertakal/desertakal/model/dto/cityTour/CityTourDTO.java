package com.desertakal.desertakal.model.dto.cityTour;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityTourDTO {
    private Integer orderIndex;
    private UUID cityUuid;
    private String cityName;
}
