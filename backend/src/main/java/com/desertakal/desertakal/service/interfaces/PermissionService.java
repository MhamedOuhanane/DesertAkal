package com.desertakal.desertakal.service.interfaces;

import com.desertakal.desertakal.model.dto.permission.PermissionDTO;
import com.desertakal.desertakal.model.dto.permission.PermissionRequestDTO;
import com.desertakal.desertakal.model.dto.permission.PermissionUpdateDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PermissionService {
    PermissionDTO create(@NonNull PermissionRequestDTO dto);
    PermissionDTO update(@NonNull UUID permissionUuid, @NonNull PermissionUpdateDTO dto);
    PaginationDTO findAll(String search, @NonNull Pageable pageable);
    PaginationDTO findByRole(String search, @NonNull String roleName, @NonNull Pageable pageable);
    void delete(@NonNull UUID permissionUuid);
}
