package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.guide.GuideCreateDTO;
import com.desertakal.desertakal.model.dto.guide.GuideDTO;
import com.desertakal.desertakal.model.dto.guide.GuideFindDTO;
import com.desertakal.desertakal.model.dto.guide.GuideUpdateDTO;
import com.desertakal.desertakal.model.entity.Guide;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {LanguageMapper.class, UserMapper.class})
public interface GuideMapper {

    GuideDTO toDto(Guide guide);

    GuideFindDTO toFindDto(Guide guide);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "languages", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    Guide toEntity(GuideCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "photo", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "languages", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    void updateEntityFromDto(GuideUpdateDTO dto, @MappingTarget Guide guide);

    List<GuideDTO> toDtos(List<Guide> guides);
}