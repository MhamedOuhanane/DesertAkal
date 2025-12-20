package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.tour.TourCreateDTO;
import com.desertakal.desertakal.model.dto.tour.TourDTO;
import com.desertakal.desertakal.model.dto.tour.TourFindDTO;
import com.desertakal.desertakal.model.dto.tour.TourUpdateDTO;
import com.desertakal.desertakal.model.entity.Tour;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CityTourMapper.class})
public interface TourMapper {

    TourDTO toDto(Tour tour);
    TourFindDTO toFindDto(Tour tour);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "cityTours", ignore = true)
    Tour toEntity(TourCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "cityTours", ignore = true)
    void updateEntityFromDto(TourUpdateDTO dto, @MappingTarget Tour tour);

    List<TourDTO> toDtos(List<Tour> tours);
    List<TourFindDTO> toFindDtos(List<Tour> tours);
}