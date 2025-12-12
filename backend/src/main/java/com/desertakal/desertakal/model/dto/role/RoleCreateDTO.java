package com.desertakal.desertakal.model.dto.role;

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
public class RoleCreateDTO {
    private String name;
    List<UUID> permissionUuids;
}
