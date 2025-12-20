package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.image.ImageDTO;
import com.desertakal.desertakal.model.entity.Image;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ImageMapper {
    @Mapping(target = "cityUuid", source = "city.uuid")
    ImageDTO toDto(Image image);
}
