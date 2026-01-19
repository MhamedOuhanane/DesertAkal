package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.exception.custom.BusinessRuleException;
import com.desertakal.desertakal.exception.custom.DuplicateResourceException;
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
import org.springframework.transaction.annotation.Transactional;

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

        if (repository.existsByName(dto.getName())) {
            log.warn("Create failed: Permission name '{}' already exists", dto.getName());
            throw new DuplicateResourceException("Permission", "name", dto.getName());
        }

        Permission permission = mapper.toEntity(dto);

        Permission newPermission = repository.save(permission);

        log.info("Permission successfully created. Assigned UUID: {} [Name: '{}']",
                newPermission.getUuid(), newPermission.getName());

        return mapper.toDto(newPermission);
    }

    @Override
    public List<PermissionDTO> createMultiple(@NonNull List<PermissionRequestDTO> requestDTOS) {
        log.info("Request to create {} new permissions", requestDTOS.size());

        List<String> names = requestDTOS.stream()
                .map(PermissionRequestDTO::getName).toList();

        if (repository.existsByNameIn(names)) {
            log.warn("Batch create failed: Some permission names already exist in {}", names);
            throw new DuplicateResourceException("One or more permissions already exist");
        }

        List<Permission> permissions = requestDTOS.stream()
                .map(mapper::toEntity)
                .toList();

        List<Permission> savedPermissions = repository.saveAll(permissions);

        log.info("Successfully created {} permissions", savedPermissions.size());

        return mapper.toDtos(savedPermissions);
    }

    @Override
    public PermissionDTO update(@NonNull UUID permissionUuid, @NonNull PermissionUpdateDTO dto) {
        log.info("Request to update Permission UUID: {} with data: {}", permissionUuid, dto.getName());

        Permission permission = repository.findByUuid(permissionUuid)
                .orElseThrow(() -> {
                    log.warn("Update failed: Permission not found for UUID: {}", permissionUuid);
                    return new ResourceNotFoundException("Permission", "identifier", permissionUuid.toString());
                });

        if (!dto.getName().equals(permission.getName()) && repository.existsByName(dto.getName())) {
            log.warn("Update failed: Permission name '{}' already exists", dto.getName());
            throw new DuplicateResourceException("Permission", "name", dto.getName());
        }

        String oldName = permission.getName();
        mapper.updateEntityFromDto(dto, permission);

        Permission updatedPermission = repository.save(permission);

        log.info("Permission '{}' (UUID: {}) updated. Name: [{} -> {}]",
                updatedPermission.getName(), permissionUuid, oldName, updatedPermission.getName());

        return mapper.toDto(updatedPermission);
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
    @Transactional
    public void delete(@NonNull UUID permissionUuid) {
        log.info("Request to delete Permission with UUID: {}", permissionUuid);

        Permission permission = repository.findByUuid(permissionUuid)
                .orElseThrow(() -> {
                    log.warn("Delete failed: Permission not found for UUID: {}", permissionUuid);
                    return new ResourceNotFoundException("Permission", "identifier", permissionUuid.toString());
                });

        var roles = permission.getRoles();
        if (!roles.isEmpty()) {
            log.warn("Security Alert: Attempt to delete Permission '{}' (UUID: {}) failed because it is still assigned to {} roles.",
                    permission.getName(), permissionUuid, permission.getRoles().size());
            throw new BusinessRuleException("Cannot delete permission: It is still assigned to " + permission.getRoles().size() + " roles.");
        }

        String permissionName = permission.getName();
        repository.delete(permission);

        log.info("Successfully deleted Permission: '{}' [UUID: {}]", permissionName, permissionUuid);
    }
}
