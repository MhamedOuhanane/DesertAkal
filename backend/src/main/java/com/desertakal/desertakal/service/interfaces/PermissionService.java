package com.desertakal.desertakal.service.interfaces;

import com.desertakal.desertakal.model.dto.permission.PermissionDTO;
import com.desertakal.desertakal.model.dto.permission.PermissionRequestDTO;
import com.desertakal.desertakal.model.dto.permission.PermissionUpdateDTO;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PermissionService {
    PermissionDTO create(@NonNull PermissionRequestDTO dto);
    PermissionDTO update(@NonNull UUID permissionUuid, @NonNull PermissionUpdateDTO dto);
    List<PermissionDTO> findAll(String search, @NonNull Pageable pageable);
    List<PermissionDTO> findByRole(String search, @NonNull UUID roleUuid, @NonNull Pageable pageable);
    void delete(@NonNull UUID permissionUuid);
}
