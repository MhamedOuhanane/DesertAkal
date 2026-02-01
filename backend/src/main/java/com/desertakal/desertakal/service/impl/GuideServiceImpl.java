package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.exception.custom.ResourceNotFoundException;
import com.desertakal.desertakal.model.dto.guide.GuideCreateDTO;
import com.desertakal.desertakal.model.dto.guide.GuideFindDTO;
import com.desertakal.desertakal.model.dto.guide.GuideUpdateDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.entity.Guide;
import com.desertakal.desertakal.model.entity.Language;
import com.desertakal.desertakal.model.mapper.GuideMapper;
import com.desertakal.desertakal.repository.GuideRepository;
import com.desertakal.desertakal.service.interfaces.GuideService;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuideServiceImpl implements GuideService {
    private final GuideRepository repository;
    private final GuideMapper mapper;

    @Override
    public GuideFindDTO create(@NonNull GuideCreateDTO dto) {
        return null;
    }

    @Override
    public PaginationDTO findAll(String search, String language, @NonNull Pageable pageable) {
        log.info("Fetching users list - Page: {}, Size: {}, Sort: {}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        Specification<@NonNull Guide> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                Expression<String> fullName = cb.concat(cb.concat(root.get("firstName"), " "), root.get("lastName"));
                predicates.add(cb.like(cb.lower(fullName), "%" + search.toLowerCase() + "%"));
            }

            if (language != null && !language.isBlank()) {
                Join<Guide, Language> languageJoin = root.join("languages");
                predicates.add(cb.like(cb.lower(languageJoin.get("name")), "%" + language.toLowerCase() + "%"));

                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        var guidePages = repository.findAll(spec, pageable);


        log.debug("Successfully retrieved {} guides from database", guidePages.getNumberOfElements());

        return PaginationDTO.builder()
                .content(mapper.toDtos(guidePages.getContent()))
                .page(guidePages.getNumber())
                .size(guidePages.getSize())
                .totalElements(guidePages.getTotalElements())
                .totalPages(guidePages.getTotalPages())
                .isFirst(guidePages.isFirst())
                .isLast(guidePages.isLast())
                .build();
    }

    @Override
    public GuideFindDTO find(@NonNull UUID guideUuid) {
        log.info("Attempting to find Guide with UUID: {}", guideUuid);

        Guide guide = repository.findByUuid(guideUuid)
                .orElseThrow(() -> {
                    log.warn("Found failed: Guide {} not found", guideUuid);
                    return new ResourceNotFoundException("Guide", "identifier", guideUuid.toString());
                });

        log.debug("Guide successfully retrieved: {} (UUID: {})", guide.getEmail(), guideUuid);

        return mapper.toFindDto(guide);
    }

    @Override
    public GuideFindDTO update(@NonNull UUID guideUuid, @NonNull GuideUpdateDTO dto) {
        return null;
    }

    @Override
    public void delete(@NonNull UUID guideUuid) {

    }
}
