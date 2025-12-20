package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.role.RoleCreateDTO;
import com.desertakal.desertakal.model.dto.role.RoleDTO;
import com.desertakal.desertakal.model.dto.role.RoleFindDTO;
import com.desertakal.desertakal.model.dto.role.RoleUpdateDTO;
import com.desertakal.desertakal.model.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {PermissionMapper.class})
public interface RoleMapper {

    RoleDTO toDto(Role role);
    RoleFindDTO toFindDto(Role role);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "users", ignore = true)
    Role toEntity(RoleCreateDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "users", ignore = true)
    Role toEntity(RoleUpdateDTO dto);
}
