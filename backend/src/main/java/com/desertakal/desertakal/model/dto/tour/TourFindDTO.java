package com.desertakal.desertakal.model.dto.tour;

import com.desertakal.desertakal.model.dto.cityTour.CityTourFindDTO;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourFindDTO {
    private UUID uuid;
    private String title;
    private String image;
    private String description;
    private Integer durationDays;
    private BigDecimal rating;
    private Integer reviewCount;
    private List<CityTourFindDTO> cityTours;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
