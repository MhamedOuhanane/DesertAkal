package com.desertakal.desertakal.model.dto.role;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

    @Size(min = 8, max = 50, message = "Role name must be between 8 and 50 characters")
    @Pattern(
            regexp = "^ROLE_[A-Z_]+$",
            message = "Role name must start with 'ROLE_' and contain only uppercase letters and underscores"
    )
    private String name;

    List<UUID> permissionUuids;
}
