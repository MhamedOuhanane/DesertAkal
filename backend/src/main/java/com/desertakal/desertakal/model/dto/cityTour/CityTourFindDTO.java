package com.desertakal.desertakal.model.dto.cityTour;

import com.desertakal.desertakal.model.dto.city.CityFindDTO;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityTourFindDTO {
    private Integer orderIndex;
    private Integer daysCount;
    private CityFindDTO city;
    private UUID tourUuid;
}
