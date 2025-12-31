package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.cityTour.CityTourCreateDTO;
import com.desertakal.desertakal.model.dto.cityTour.CityTourDTO;
import com.desertakal.desertakal.model.dto.cityTour.CityTourFindDTO;
import com.desertakal.desertakal.model.entity.CityTour;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CityMapper.class})
public interface CityTourMapper {

    @Named("toDto")
    @Mapping(source = "city.uuid", target = "cityUuid")
    @Mapping(source = "city.name", target = "cityName")
    CityTourDTO toDto(CityTour cityTour);

    @Mapping(source = "tour.uuid", target = "tourUuid")
    CityTourFindDTO toFindDto(CityTour cityTour);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "city", ignore = true)
    @Mapping(target = "tour", ignore = true)
    CityTour toEntity(CityTourCreateDTO dto);

    @IterableMapping(qualifiedByName = "toDto")
    List<CityTourDTO> toDtos(List<CityTour> cityTours);
}
