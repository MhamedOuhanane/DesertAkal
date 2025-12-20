package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.tourist.TouristDTO;
import com.desertakal.desertakal.model.entity.Tourist;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface TouristMapper {

    @InheritConfiguration(name = "toFindDto")
    TouristDTO toDto(Tourist tourist);

    List<TouristDTO> toDtos(List<Tourist> tourists);
}
