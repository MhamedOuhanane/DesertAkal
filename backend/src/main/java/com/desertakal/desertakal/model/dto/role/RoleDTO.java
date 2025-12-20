package com.desertakal.desertakal.model.dto.role;

import com.desertakal.desertakal.model.dto.permission.PermissionDTO;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RoleDTO {
    private UUID uuid;
    private String name;
}
