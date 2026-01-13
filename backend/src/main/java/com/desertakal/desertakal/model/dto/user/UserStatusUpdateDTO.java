package com.desertakal.desertakal.model.dto.user;

import com.desertakal.desertakal.model.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusUpdateDTO {

    @NotNull(message = "Status cannot be null")
    private UserStatus status;
}
