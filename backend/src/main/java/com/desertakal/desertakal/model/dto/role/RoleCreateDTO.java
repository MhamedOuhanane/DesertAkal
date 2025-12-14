package com.desertakal.desertakal.model.dto.role;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RoleCreateDTO {
    private String name;
    List<UUID> permissionUuids;
}
