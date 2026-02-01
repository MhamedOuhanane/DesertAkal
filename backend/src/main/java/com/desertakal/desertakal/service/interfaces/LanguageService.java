package com.desertakal.desertakal.service.interfaces;

import com.desertakal.desertakal.model.dto.language.LanguageCreateDTO;
import com.desertakal.desertakal.model.dto.language.LanguageDTO;
import com.desertakal.desertakal.model.dto.language.LanguageUpdateDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface LanguageService {
    LanguageDTO create(@NonNull LanguageCreateDTO dto);
    PaginationDTO findAll(String search, @NonNull Pageable pageable);
    LanguageDTO find(@NonNull UUID languageUuid);
    LanguageDTO update(@NonNull UUID languageUuid, @NonNull LanguageUpdateDTO dto);
    void delete(@NonNull UUID languageUuid);
}
