package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.guide.GuideCreateDTO;
import com.desertakal.desertakal.model.dto.guide.GuideDTO;
import com.desertakal.desertakal.model.dto.guide.GuideFindDTO;
import com.desertakal.desertakal.model.dto.guide.GuideUpdateDTO;
import com.desertakal.desertakal.model.entity.Guide;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {LanguageMapper.class, UserMapper.class},
        builder = @Builder(disableBuilder = true)
)
public interface GuideMapper {

    @Named("toDto")
    @Mapping(source = "role.name", target = "role")
    GuideDTO toDto(Guide guide);

    @Mapping(source = "role.name", target = "role")
    GuideFindDTO toFindDto(Guide guide);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "photo", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "languages", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    Guide toEntity(GuideCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @InheritConfiguration(name = "toEntity")
    void updateEntityFromDto(GuideUpdateDTO dto, @MappingTarget Guide guide);

    @IterableMapping(qualifiedByName = "toDto")
    List<GuideDTO> toDtos(List<Guide> guides);
}