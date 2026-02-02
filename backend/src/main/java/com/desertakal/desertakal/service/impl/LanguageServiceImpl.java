package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.exception.custom.BusinessRuleException;
import com.desertakal.desertakal.exception.custom.DuplicateResourceException;
import com.desertakal.desertakal.exception.custom.ResourceNotFoundException;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LanguageServiceImpl implements LanguageService {
    private final LanguageRepository repository;
    private final LanguageMapper mapper;

    @Override
    public LanguageDTO create(@NonNull LanguageCreateDTO dto) {
        log.info("Request to create new Language with name: '{}'", dto.getName());

        if (repository.existsByName(dto.getName())) {
            log.warn("Create failed: Language name '{}' already exists", dto.getName());
            throw new DuplicateResourceException("Language", "name", dto.getName());
        }

        if (repository.existsByCode(dto.getCode())) {
            log.warn("Create failed: Language code '{}' already exists", dto.getCode());
            throw new DuplicateResourceException("Language", "code", dto.getCode());
        }

        Language language = mapper.toEntity(dto);

        Language newLanguage = repository.save(language);

        log.info("Language successfully created. Assigned UUID: {} [Name: '{}']",
                newLanguage.getUuid(), newLanguage.getName());

        return mapper.toDto(newLanguage);
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
        log.info("Fetching details for Language UUID: '{}'", languageUuid);

        Language language = repository.findByUuid(languageUuid)
                .orElseThrow(() -> {
                    log.warn("Lookup failed: Language UUID '{}' not found in database", languageUuid);
                    return new ResourceNotFoundException("Language", "identifier", languageUuid.toString());
                });

        log.info("Successfully found Language: '{}' ", language.getName());

        return mapper.toDto(language);
    }

    @Override
    public LanguageDTO update(@NonNull UUID languageUuid, @NonNull LanguageUpdateDTO dto) {
        log.info("Request to update Language UUID: {} with data: {}", languageUuid, dto.getName());

        Language language = repository.findByUuid(languageUuid)
                .orElseThrow(() -> {
                    log.warn("Update failed: Language not found for UUID: {}", languageUuid);
                    return new ResourceNotFoundException("Language", "identifier", languageUuid.toString());
                });

        if (dto.getName() != null && !dto.getName().equals(language.getName()) && repository.existsByName(dto.getName())) {
            log.warn("Update failed: Language name '{}' already exists", dto.getName());
            throw new DuplicateResourceException("Language", "name", dto.getName());
        }

        if (dto.getCode() != null && !dto.getCode().equals(language.getCode()) && repository.existsByCode(dto.getCode())) {
            log.warn("Update failed: Language code '{}' already exists", dto.getCode());
            throw new DuplicateResourceException("Language", "code", dto.getCode());
        }

        mapper.updateEntityFromDto(dto, language);

        Language updatedLanguage = repository.save(language);

        log.info("Language '{}' (UUID: {}) updated. Name: [{} -> {}]",
                updatedLanguage.getName(), languageUuid, updatedLanguage.getName(), language.getName());

        return mapper.toDto(updatedLanguage);
    }

    @Override
    @Transactional
    public void delete(@NonNull UUID languageUuid) {
        log.info("Request to delete Language with UUID: {}", languageUuid);

        Language language = repository.findByUuid(languageUuid)
                .orElseThrow(() -> {
                    log.warn("Delete failed: Language not found for UUID: {}", languageUuid);
                    return new ResourceNotFoundException("Language", "identifier", languageUuid.toString());
                });

        var guides = language.getGuides();
        if (!guides.isEmpty()) {
            log.warn("Security Alert: Attempt to delete Language '{}' (UUID: {}) failed because it is still assigned to {} guides.",
                    language.getName(), languageUuid, language.getGuides().size());
            throw new BusinessRuleException("Cannot delete language: It is still assigned to " + language.getGuides().size() + " guides.");
        }

        String languageName = language.getName();
        repository.delete(language);

        log.info("Successfully deleted Language: '{}' [UUID: {}]", languageName, languageUuid);
    }
}
