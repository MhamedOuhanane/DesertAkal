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

    @Named("toDto")
    TourDTO toDto(Tour tour);
    TourFindDTO toFindDto(Tour tour);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "durationDays", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "cityTours", ignore = true)
    Tour toEntity(TourCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @InheritConfiguration(name = "toEntity")
    void updateEntityFromDto(TourUpdateDTO dto, @MappingTarget Tour tour);

    @IterableMapping(qualifiedByName = "toDto")
    List<TourDTO> toDtos(List<Tour> tours);
}