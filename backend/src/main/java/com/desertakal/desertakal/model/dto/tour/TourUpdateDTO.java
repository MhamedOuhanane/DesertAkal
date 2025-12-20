package com.desertakal.desertakal.model.dto.tour;

import com.desertakal.desertakal.model.dto.cityTour.CityTourCreateDTO;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourUpdateDTO {
    private String title;
    private String description;
    private List<CityTourCreateDTO> cityTours;

}
