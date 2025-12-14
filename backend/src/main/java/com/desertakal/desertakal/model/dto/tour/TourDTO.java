package com.desertakal.desertakal.model.dto.tour;

import com.desertakal.desertakal.model.dto.cityTour.CityTourDTO;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourDTO {
    private UUID uuid;
    private String title;
    private String image;
    private Integer durationDays;
    private BigDecimal rating;
    private Integer reviewCount;
    private List<CityTourDTO> cityTours;

}
