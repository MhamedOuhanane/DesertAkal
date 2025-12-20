package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.language.LanguageCreateDTO;
import com.desertakal.desertakal.model.dto.language.LanguageDTO;
import com.desertakal.desertakal.model.dto.language.LanguageUpdateDTO;
import com.desertakal.desertakal.model.entity.Language;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LanguageMapper {

    LanguageDTO toDto(Language language);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "guides", ignore = true)
    Language toEntity(LanguageCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "guides", ignore = true)
    void updateEntityFromDto(LanguageUpdateDTO dto, @MappingTarget Language language);

    List<LanguageDTO> toDtos(List<Language> languages);
}
