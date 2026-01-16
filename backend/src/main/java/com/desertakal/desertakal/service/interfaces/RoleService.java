package com.desertakal.desertakal.service.interfaces;

import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.dto.role.RoleCreateDTO;
import com.desertakal.desertakal.model.dto.role.RoleFindDTO;
import com.desertakal.desertakal.model.dto.role.RoleUpdateDTO;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface RoleService {
    PaginationDTO findAll(String search, @NonNull Pageable pageable);
    RoleFindDTO create(@NonNull RoleCreateDTO dto);
    RoleFindDTO update(@NonNull UUID roleUuid, @NonNull RoleUpdateDTO dto);
    RoleFindDTO find(@NonNull UUID roleName);
    void delete(@NonNull UUID roleUuid);
}
