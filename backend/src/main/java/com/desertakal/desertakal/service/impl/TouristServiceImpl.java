package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.entity.Tourist;
import com.desertakal.desertakal.model.enums.UserStatus;
import com.desertakal.desertakal.model.mapper.TouristMapper;
import com.desertakal.desertakal.repository.TouristRepository;
import com.desertakal.desertakal.service.interfaces.TouristService;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TouristServiceImpl implements TouristService {
    private final TouristRepository repository;
    private final TouristMapper mapper;

    @Override
    public PaginationDTO findAll(String search, UserStatus status, String nationality, @NonNull Pageable pageable) {
        log.info("Fetching tourists list - Page: {}, Size: {}, Sort: {}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        Specification<@NonNull Tourist> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null)
                predicates.add(cb.equal(root.get("status"), status));

            if (nationality != null && !nationality.isBlank())
                predicates.add(cb.like(cb.lower(root.get("nationality")), "%" + nationality.toLowerCase() + "%"));

            if (search != null && !search.isBlank()) {
                Expression<String> fullName = cb.concat(cb.concat(root.get("firstName"), " "), root.get("lastName"));
                predicates.add(cb.like(cb.lower(fullName), "%" + search.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        var touristPages = repository.findAll(spec, pageable);

        log.debug("Successfully retrieved {} tourists from database", touristPages.getNumberOfElements());

        return PaginationDTO.builder()
                .content(mapper.toDtos(touristPages.getContent()))
                .page(touristPages.getNumber())
                .size(touristPages.getSize())
                .totalElements(touristPages.getTotalElements())
                .totalPages(touristPages.getTotalPages())
                .isFirst(touristPages.isFirst())
                .isLast(touristPages.isLast())
                .build();
    }
}
