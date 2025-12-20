package com.desertakal.desertakal.model.dto.role;

import com.desertakal.desertakal.model.dto.permission.PermissionDTO;
import com.desertakal.desertakal.model.entity.Permission;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RoleFindDTO {
    private UUID uuid;
    private String name;
    List<PermissionDTO> permissions;
}
