package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.exception.custom.BusinessRuleException;
import com.desertakal.desertakal.exception.custom.ResourceNotFoundException;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.dto.role.RoleCreateDTO;
import com.desertakal.desertakal.model.dto.role.RoleFindDTO;
import com.desertakal.desertakal.model.dto.role.RoleUpdateDTO;
import com.desertakal.desertakal.model.entity.Role;
import com.desertakal.desertakal.model.mapper.RoleMapper;
import com.desertakal.desertakal.repository.PermissionRepository;
import com.desertakal.desertakal.repository.RoleRepository;
import com.desertakal.desertakal.service.interfaces.RoleService;
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
public class RoleServiceImpl implements RoleService {
    private final RoleRepository repository;
    private final RoleMapper mapper;
    private final PermissionRepository permissionRepository;

    @Override
    public PaginationDTO findAll(String search, @NonNull Pageable pageable) {
        log.info("Fetching roles with search: {}", search);

        Specification<@NonNull Role> spec = (root, query, cb) -> {
            if (search == null || search.isEmpty()) {
                return cb.conjunction();
            }

            String pattern = "%" + search.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("name")), pattern);
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
        log.info("Starting creation of new Role: '{}' with {} permissions",
                dto.getName(), (dto.getPermissionUuids() != null ? dto.getPermissionUuids().size() : 0));

        Role role = mapper.toEntity(dto);

        var permissions = permissionRepository.findDistinctByUuidIn(dto.getPermissionUuids());

        role.setPermissions(permissions);

        Role savedRole = repository.save(role);

        log.info("Successfully created Role: '{}' with UUID: {}", savedRole.getName(), savedRole.getUuid());

        return mapper.toFindDto(savedRole);
    }

    @Override
    @Transactional
    public RoleFindDTO update(@NonNull String roleName, @NonNull RoleUpdateDTO dto) {
        log.info("Request to update Role name: {} with data: {}", roleName, dto.getName());

        Role role = repository.findByName(roleName)
            .orElseThrow(() -> {
                log.warn("Update failed: Role not found for name: {}", roleName);
                return new ResourceNotFoundException("Role", "name", roleName);
            });

        String oldName = role.getName();
        int oldPermissionsCount = role.getPermissions().size();

        mapper.updateEntityFromDto(dto, role);

        if (dto.getPermissionUuids() != null) {
            log.debug("Updating permissions for role '{}'. New count requested: {}", role.getName(), dto.getPermissionUuids().size());

            var newPermissions = permissionRepository.findDistinctByUuidIn(dto.getPermissionUuids());

            if (newPermissions.size() != dto.getPermissionUuids().size()) {
                log.warn("Missing Permissions! Requested: {}, Found: {}", dto.getPermissionUuids().size(), newPermissions.size());
            }

            role.getPermissions().clear();
            role.getPermissions().addAll(newPermissions);
        }

        Role updatedRole = repository.save(role);

        log.info("Role '{}' (UUID: {}) updated. Name: [{} -> {}], Permissions: [{} -> {}]",
                roleUuid, updatedRole.getName(), oldName, updatedRole.getName(),
                oldPermissionsCount, updatedRole.getPermissions().size());

        return mapper.toFindDto(updatedRole);
    }

    @Override
    @Transactional
    public void delete(@NonNull String roleName) {
        log.info("Request to delete Role with name: {}", roleName);

        Role role = repository.findByName(roleName)
                .orElseThrow(() -> {
                    log.warn("Delete failed: Role not found for name: {}", roleName);
                    return new ResourceNotFoundException("Role", "name", roleName);
                });

        var users = role.getUsers();
        if (!users.isEmpty()) {
            if (!role.getUsers().isEmpty()) {
                log.warn("Security Alert: Attempt to delete Role '{}' (UUID: {}) failed because it is still assigned to {} users.",
                        roleName, role.getUuid(), role.getUsers().size());
                throw new BusinessRuleException("Cannot delete role: It is still assigned to " + role.getUsers().size() + " users.");
            }
        }

        UUID roleUuid = role.getUuid();
        repository.delete(role);

        log.info("Successfully deleted Role: '{}' [UUID: {}]", roleName, roleUuid);
    }
}
