package com.desertakal.desertakal.model.dto.refreshToken;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemoteLogoutRequestDTO {
    @NotNull(message = "The session Uuid is required")
    private UUID sessionUuid;

    @NotBlank(message = "A password is required to confirm this action.")
    private String password;
}

