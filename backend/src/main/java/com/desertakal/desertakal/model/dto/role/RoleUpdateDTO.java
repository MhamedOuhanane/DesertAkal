package com.desertakal.desertakal.model.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
public class RoleUpdateDTO {
    @Size(min = 4, max = 50, message = "Role name must be between 4 and 50 characters")
    @Pattern(
            regexp = "^[A-Z_]+$",
            message = "Role name contain only uppercase letters and underscores"
    )
    private String name;

    private List<UUID> permissionUuids;
}
