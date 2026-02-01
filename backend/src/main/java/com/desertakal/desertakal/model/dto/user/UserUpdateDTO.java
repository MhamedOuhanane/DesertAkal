package com.desertakal.desertakal.model.dto.user;

import com.desertakal.desertakal.model.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO {
    @Size(min = 3, max = 50, message = "First name must be between 3 and 50 characters")
    protected String firstName;

    @Size(min = 3, max = 50, message = "Last name must be between 3 and 50 characters")
    protected String lastName;

    @Pattern(
            regexp = "^(\\+\\d{1,3}[- ]?)?\\d{6,15}$",
            message = "Phone number is invalid"
    )
    protected String phone;


}
