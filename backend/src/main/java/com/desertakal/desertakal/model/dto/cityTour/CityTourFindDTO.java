package com.desertakal.desertakal.model.dto.cityTour;

import com.desertakal.desertakal.model.entity.City;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityTourFindDTO {
    private Integer orderIndex;
    private City city;
    private UUID tourUuid;
}
