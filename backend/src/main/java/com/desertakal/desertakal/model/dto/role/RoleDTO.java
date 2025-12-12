package com.desertakal.desertakal.model.dto.role;

import com.desertakal.desertakal.model.dto.permission.PermissionDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
    private UUID uuid;
    private String name;
    List<PermissionDTO> permissions;
}
