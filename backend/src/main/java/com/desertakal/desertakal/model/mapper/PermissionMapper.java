package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.permission.PermissionDTO;
import com.desertakal.desertakal.model.dto.permission.PermissionRequestDTO;
import com.desertakal.desertakal.model.entity.Permission;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    PermissionDTO toDto(Permission permission);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "roles", ignore = true)
    Permission toEntity(PermissionRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @InheritConfiguration(name = "toEntity")
    void updateEntityFromDto(PermissionRequestDTO dto, @MappingTarget Permission permission);

    List<PermissionDTO> toDtos(List<Permission> permissions);
}
