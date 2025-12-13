package com.desertakal.desertakal.model.dto.user;

import com.desertakal.desertakal.model.dto.role.RoleFindDTO;
import com.desertakal.desertakal.model.enums.OauthProvider;
import com.desertakal.desertakal.model.enums.UserStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    protected UUID uuid;

    protected String firstName;
    protected String lastName;
    protected String username;
    protected String email;

    protected String phone;
    protected String photo;

    protected UserStatus status;

    protected LocalDateTime lastLoginAt;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;

    protected RoleFindDTO role;

    private List<String> oauthProviders;
}
