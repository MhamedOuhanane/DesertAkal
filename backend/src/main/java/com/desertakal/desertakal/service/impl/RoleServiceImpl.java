package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.dto.role.RoleCreateDTO;
import com.desertakal.desertakal.model.dto.role.RoleDTO;
import com.desertakal.desertakal.model.dto.role.RoleFindDTO;
import com.desertakal.desertakal.model.dto.role.RoleUpdateDTO;
import com.desertakal.desertakal.model.entity.Role;
import com.desertakal.desertakal.model.mapper.RoleMapper;
import com.desertakal.desertakal.repository.RoleRepository;
import com.desertakal.desertakal.service.interfaces.RoleService;
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
public class RoleServiceImpl implements RoleService {
    private final RoleRepository repository;
    private final RoleMapper mapper;

    @Override
    public PaginationDTO findAll(String search, @NonNull Pageable pageable) {
        log.info("Fetching roles with search: {}", search);

        Specification<@NonNull Role> spec = (root, query, cb) -> {
            if (search == null || search.isEmpty()) {
                return cb.conjunction();
            }

            String pattern = "%" + search.toLowerCase() + "%";
            return cb.like(root.get("name"), pattern);
        };

        var roles = repository.findAll(spec, pageable);

        log.debug("Found {} roles in current page", roles.getNumberOfElements());

        return PaginationDTO.builder()
                .content(mapper.toDtos(roles.getContent()))
                .page(roles.getNumber())
                .size(roles.getSize())
                .totalElements(roles.getTotalElements())
                .totalPages(roles.getTotalPages())
                .isFirst(roles.isFirst())
                .isLast(roles.isLast())
                .build();
    }

    @Override
    public RoleFindDTO create(@NonNull RoleCreateDTO dto) {
        return null;
    }

    @Override
    public RoleFindDTO update(@NonNull UUID roleUuid, @NonNull RoleUpdateDTO dto) {
        return null;
    }

    @Override
    public void delete(@NonNull UUID roleUuid) {

    }
}
