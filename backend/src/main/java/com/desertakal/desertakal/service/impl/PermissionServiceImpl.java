package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.exception.custom.ResourceNotFoundException;
import com.desertakal.desertakal.model.dto.permission.PermissionDTO;
import com.desertakal.desertakal.model.dto.permission.PermissionRequestDTO;
import com.desertakal.desertakal.model.dto.permission.PermissionUpdateDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.entity.Permission;
import com.desertakal.desertakal.model.entity.Role;
import com.desertakal.desertakal.model.mapper.PermissionMapper;
import com.desertakal.desertakal.repository.PermissionRepository;
import com.desertakal.desertakal.repository.RoleRepository;
import com.desertakal.desertakal.service.interfaces.PermissionService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionServiceImpl implements PermissionService {
    private final PermissionRepository repository;
    private final PermissionMapper mapper;
    private final RoleRepository roleRepository;

    @Override
    public PermissionDTO create(@NonNull PermissionRequestDTO dto) {
        log.info("Request to create new Permission with name: '{}'", dto.getName());

        Permission permission = mapper.toEntity(dto);

        Permission newPermission = repository.save(permission);

        log.info("Permission successfully created. Assigned UUID: {} [Name: '{}']",
                newPermission.getUuid(), newPermission.getName());

        return mapper.toDto(newPermission);
    }

    @Override
    public List<PermissionDTO> createMultiple(@NonNull List<PermissionRequestDTO> requestDTOS) {
        log.info("Request to create {} new permissions", requestDTOS.size());

        List<Permission> permissions = requestDTOS.stream()
                .map(mapper::toEntity)
                .toList();

        List<Permission> savedPermissions = repository.saveAll(permissions);

        log.info("Successfully created {} permissions", savedPermissions.size());

        return mapper.toDtos(savedPermissions);
    }

    @Override
    public PermissionDTO update(@NonNull UUID permissionUuid, @NonNull PermissionUpdateDTO dto) {
        return null;
    }

    @Override
    public PaginationDTO findAll(String search, @NonNull Pageable pageable) {
        log.info("Fetching permissions : [Search: '{}', Page: {}]",
                 search != null ? search : "NONE", pageable.getPageNumber());

        Specification<@NonNull Permission> spec = (root, query, cb) -> {
            if (search != null && !search.isEmpty()) {
                String pattern = "%" + search.toLowerCase() + "%";
                return cb.like(cb.lower(root.get("name")), pattern);
            }

            return cb.conjunction();
        };

        var permissionsPage = repository.findAll(spec, pageable);

        log.info("Success: Retrieved {} permissions (Total elements in DB: {})",
                permissionsPage.getNumberOfElements(),
                permissionsPage.getTotalElements());

        return PaginationDTO.builder()
                .content(mapper.toDtos(permissionsPage.getContent()))
                .page(permissionsPage.getNumber())
                .size(permissionsPage.getSize())
                .totalElements(permissionsPage.getTotalElements())
                .totalPages(permissionsPage.getTotalPages())
                .isFirst(permissionsPage.isFirst())
                .isLast(permissionsPage.isLast())
                .build();
    }

    @Override
    public PaginationDTO findByRole(String search, @NonNull String roleName, @NonNull Pageable pageable) {
        log.info("Fetching permissions for role: '{}' [Search: '{}', Page: {}]",
                roleName, search != null ? search : "NONE", pageable.getPageNumber());

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> {
                    log.warn("Lookup failed: Role name '{}' does not exist in database", roleName);
                    return new ResourceNotFoundException("Role", "name", roleName);
                });

        Specification<@NonNull Permission> spec = (root, query, cb) -> {
            Join<Permission, Role> roleJoin = root.join("roles");
            Predicate rolePredicate =cb.equal(roleJoin.get("uuid"), role.getUuid());

            if (search != null && !search.isEmpty()) {
                String pattern = "%" + search.toLowerCase() + "%";
                Predicate searchPredicate = cb.like(cb.lower(root.get("name")), pattern);
                return cb.and(rolePredicate, searchPredicate);
            }

            return rolePredicate;
        };

        var permissionsPage = repository.findAll(spec, pageable);

        log.info("Success: Retrieved {} permissions for role '{}' (Total elements in DB: {})",
                permissionsPage.getNumberOfElements(),
                role.getName(),
                permissionsPage.getTotalElements());

        return PaginationDTO.builder()
                .content(mapper.toDtos(permissionsPage.getContent()))
                .page(permissionsPage.getNumber())
                .size(permissionsPage.getSize())
                .totalElements(permissionsPage.getTotalElements())
                .totalPages(permissionsPage.getTotalPages())
                .isFirst(permissionsPage.isFirst())
                .isLast(permissionsPage.isLast())
                .build();
    }

    @Override
    public void delete(@NonNull UUID permissionUuid) {

    }
}
