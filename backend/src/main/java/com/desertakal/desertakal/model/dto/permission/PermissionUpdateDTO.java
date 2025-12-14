package com.desertakal.desertakal.model.dto.permission;

import lombok.*;

import java.util.UUID;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PermissionUpdateDTO {
    private UUID uuid;
    private String name;
}
