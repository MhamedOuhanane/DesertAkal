package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.model.dto.language.LanguageCreateDTO;
import com.desertakal.desertakal.model.dto.language.LanguageDTO;
import com.desertakal.desertakal.model.dto.language.LanguageUpdateDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.entity.Language;
import com.desertakal.desertakal.model.mapper.LanguageMapper;
import com.desertakal.desertakal.repository.LanguageRepository;
import com.desertakal.desertakal.service.interfaces.LanguageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LanguageServiceImpl implements LanguageService {
    private final LanguageRepository repository;
    private final LanguageMapper mapper;

    @Override
    public LanguageDTO create(@NonNull LanguageCreateDTO dto) {
        return null;
    }

    @Override
    public PaginationDTO findAll(String search, @NonNull Pageable pageable) {
        log.info("Fetching languages : [Search: '{}', Page: {}]",
                search != null ? search : "NONE", pageable.getPageNumber());

        Specification<@NonNull Language> spec = (root, query, cb) -> {
            if (search != null && !search.isEmpty()) {
                String pattern = "%" + search.toLowerCase() + "%";
                return cb.like(cb.lower(root.get("name")), pattern);
            }

            return cb.conjunction();
        };

        var languagePages = repository.findAll(spec, pageable);

        log.info("Success: Retrieved {} language (Total elements in DB: {})",
                languagePages.getNumberOfElements(),
                languagePages.getTotalElements());

        return PaginationDTO.builder()
                .content(mapper.toDtos(languagePages.getContent()))
                .page(languagePages.getNumber())
                .size(languagePages.getSize())
                .totalElements(languagePages.getTotalElements())
                .totalPages(languagePages.getTotalPages())
                .isFirst(languagePages.isFirst())
                .isLast(languagePages.isLast())
                .build();
    }

    @Override
    public LanguageDTO find(@NonNull UUID languageUuid) {
        return null;
    }

    @Override
    public LanguageDTO update(@NonNull UUID languageUuid, @NonNull LanguageUpdateDTO dto) {
        return null;
    }

    @Override
    public void delete(@NonNull UUID languageUuid) {

    }
}
