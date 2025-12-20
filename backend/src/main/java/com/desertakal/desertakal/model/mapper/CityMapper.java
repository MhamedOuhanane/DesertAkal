package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.city.CityCreateDTO;
import com.desertakal.desertakal.model.dto.city.CityDTO;
import com.desertakal.desertakal.model.dto.city.CityFIndDTO;
import com.desertakal.desertakal.model.entity.City;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ImageMapper.class})
public interface CityMapper {

    CityDTO toDto(City city);
    CityFIndDTO toFindDto(City city);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "cityTours", ignore = true)
    City toEntity(CityCreateDTO dto);

    List<CityDTO> toDtos(List<City> cities);
    List<CityFIndDTO> toFindDtos(List<City> cities);
}
