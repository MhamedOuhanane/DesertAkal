package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.guide.GuideCreateDTO;
import com.desertakal.desertakal.model.dto.guide.GuideDTO;
import com.desertakal.desertakal.model.dto.guide.GuideFindDTO;
import com.desertakal.desertakal.model.dto.guide.GuideUpdateDTO;
import com.desertakal.desertakal.model.entity.Guide;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {LanguageMapper.class, UserMapper.class})
public interface GuideMapper {

    @InheritConfiguration(name = "toDto")
    GuideDTO toDto(Guide guide);

    @InheritConfiguration(name = "toFindDto")
    GuideFindDTO toFindDto(Guide guide);

    @InheritConfiguration(name = "toEntity")
    @Mapping(target = "languages", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    Guide toEntity(GuideCreateDTO dto);

    @InheritConfiguration(name = "updateEntityFromDto")
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "languages", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    void updateEntityFromDto(GuideUpdateDTO dto, @MappingTarget Guide guide);

    List<GuideDTO> toDtos(List<Guide> guides);
    List<GuideFindDTO> toFindDtos(List<Guide> guides);
}
