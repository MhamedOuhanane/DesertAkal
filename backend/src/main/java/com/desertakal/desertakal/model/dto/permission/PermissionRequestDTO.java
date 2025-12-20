package com.desertakal.desertakal.model.dto.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PermissionRequestDTO {
    @NotBlank(message = "Permission name is required")
    @Size(min = 5, max = 50, message = "Permission name must be between 5 and 50 characters")
    private String name;
}
