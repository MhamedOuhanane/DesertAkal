package com.desertakal.desertakal.model.dto.user;

import com.desertakal.desertakal.model.dto.role.RoleFindDTO;
import com.desertakal.desertakal.model.enums.UserStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserFindDTO extends UserDTO {

    protected String phone;

    protected UserStatus status;

    protected LocalDateTime lastLoginAt;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;

    private List<String> oauthProviders;
}
