package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.language.LanguageCreateDTO;
import com.desertakal.desertakal.model.dto.language.LanguageDTO;
import com.desertakal.desertakal.model.entity.Language;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LanguageMapper {

    LanguageDTO toDto(Language language);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "guides", ignore = true)
    Language toEntity(LanguageCreateDTO dto);

    List<LanguageDTO> toDtos(List<Language> languages);
}
