package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.model.dto.permission.PermissionDTO;
import com.desertakal.desertakal.model.dto.permission.PermissionRequestDTO;
import com.desertakal.desertakal.model.dto.permission.PermissionUpdateDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.service.interfaces.PermissionService;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public class PermissionServiceImpl implements PermissionService {
    @Override
    public PermissionDTO create(@NonNull PermissionRequestDTO dto) {
        return null;
    }

    @Override
    public PermissionDTO update(@NonNull UUID permissionUuid, @NonNull PermissionUpdateDTO dto) {
        return null;
    }

    @Override
    public PaginationDTO findAll(String search, @NonNull Pageable pageable) {
        return null;
    }

    @Override
    public PaginationDTO findByRole(String search, @NonNull UUID roleUuid, @NonNull Pageable pageable) {
        return null;
    }

    @Override
    public void delete(@NonNull UUID permissionUuid) {

    }
}
