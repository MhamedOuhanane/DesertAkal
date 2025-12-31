package com.desertakal.desertakal.model.dto.auth;

import com.desertakal.desertakal.model.enums.OauthProvider;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RegisterDTO {
    @NotBlank(message = "First name is required")
    @Size(min = 3, max = 50, message = "First name must be between 3 and 50 characters")
    protected String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 3, max = 50, message = "Last name must be between 3 and 50 characters")
    protected String lastName;

    @NotBlank
    @Size(min = 7, max = 20, message = "Username must be at least 7 characters")
    @Pattern(
            regexp = "^[a-zA-Z0-9._-]+$",
            message = "Username can contain letters, numbers, dot, underscore and dash only"
    )
    protected String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    protected String email;

    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter and one number"
    )
    protected String password;

    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter and one number"
    )
    protected String confirmPassword;

    protected UUID roleUuid;

    @Size(max = 50)
    protected String nationality;

    @Size(max = 50)
    protected String language;

    protected OauthProvider oauthProvider;
    protected String providerId;
}
