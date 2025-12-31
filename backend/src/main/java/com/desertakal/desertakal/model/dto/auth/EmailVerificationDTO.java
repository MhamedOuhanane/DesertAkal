package com.desertakal.desertakal.model.dto.auth;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    protected String email;

}
