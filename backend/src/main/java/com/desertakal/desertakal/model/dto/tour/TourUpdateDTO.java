package com.desertakal.desertakal.model.dto.tour;

import com.desertakal.desertakal.model.dto.cityTour.CityTourCreateDTO;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourUpdateDTO {
    @Size(min = 4, max = 100, message = "Title must be between 4 and 100 characters")
    private String title;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;

    private List<CityTourCreateDTO> cityTours;

}
