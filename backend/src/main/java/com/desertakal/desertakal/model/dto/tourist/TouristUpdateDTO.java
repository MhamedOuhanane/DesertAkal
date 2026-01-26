package com.desertakal.desertakal.model.dto.tourist;

import com.desertakal.desertakal.model.dto.user.UserDTO;
import com.desertakal.desertakal.model.dto.user.UserUpdateDTO;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TouristUpdateDTO extends UserUpdateDTO {
    @Size(max = 50, message = "Nationality name is too long")
    private String nationality;

    @Size(max = 20, message = "Language code or name is too long")
    private String language;
}
