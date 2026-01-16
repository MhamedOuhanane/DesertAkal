package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.model.dto.role.RoleCreateDTO;
import com.desertakal.desertakal.model.dto.role.RoleDTO;
import com.desertakal.desertakal.model.dto.role.RoleFindDTO;
import com.desertakal.desertakal.model.dto.role.RoleUpdateDTO;
import com.desertakal.desertakal.service.interfaces.RoleService;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public class RoleServiceImpl implements RoleService {
    @Override
    public List<RoleDTO> findAll(String search, @NonNull Pageable pageable) {
        return List.of();
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
