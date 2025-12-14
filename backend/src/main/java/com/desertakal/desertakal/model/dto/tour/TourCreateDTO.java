package com.desertakal.desertakal.model.dto.tour;

import com.desertakal.desertakal.model.dto.cityTour.CityTourCreateDTO;
import com.desertakal.desertakal.model.entity.CityTour;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourCreateDTO {
    private String title;
    private String description;
    private String image;
    private List<CityTourCreateDTO> cityTours;

}
