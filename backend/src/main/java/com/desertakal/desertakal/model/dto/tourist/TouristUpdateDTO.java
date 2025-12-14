package com.desertakal.desertakal.model.dto.tourist;

import com.desertakal.desertakal.model.dto.user.UserDTO;
import com.desertakal.desertakal.model.dto.user.UserUpdateDTO;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TouristUpdateDTO extends UserUpdateDTO {
    private String avatarUrl;
    private String nationality;
    private String language;
}
