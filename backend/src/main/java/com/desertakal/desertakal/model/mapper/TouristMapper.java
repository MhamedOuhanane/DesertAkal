package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.tourist.TouristDTO;
import com.desertakal.desertakal.model.dto.tourist.TouristUpdateDTO;
import com.desertakal.desertakal.model.entity.Tourist;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface TouristMapper {

    @InheritConfiguration(name = "toFindDto")
    TouristDTO toDto(Tourist tourist);

    @InheritConfiguration(name = "updateEntityFromDto")
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    void updateEntityFromDto(TouristUpdateDTO dto, @MappingTarget Tourist tourist);

    List<TouristDTO> toDtos(List<Tourist> tourists);
}
