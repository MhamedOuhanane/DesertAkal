package com.desertakal.desertakal.service.interfaces;

import com.desertakal.desertakal.model.dto.role.RoleCreateDTO;
import com.desertakal.desertakal.model.dto.role.RoleDTO;
import com.desertakal.desertakal.model.dto.role.RoleFindDTO;
import com.desertakal.desertakal.model.dto.role.RoleUpdateDTO;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface RoleService {
    List<RoleDTO> findAll(String search, @NonNull Pageable pageable);
    RoleFindDTO create(@NonNull RoleCreateDTO dto);
    RoleFindDTO update(@NonNull UUID roleUuid, @NonNull RoleUpdateDTO dto);
    void delete(@NonNull UUID roleUuid);
}
