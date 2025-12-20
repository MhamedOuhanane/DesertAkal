package com.desertakal.desertakal.model.mapper;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {CityMapper.class})
public interface CityTourMapper {
}
