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
public class UserUpdateDTO {
    protected String firstName;
    protected String lastName;
    protected String email;

    protected String phone;
    protected String photo;
}
