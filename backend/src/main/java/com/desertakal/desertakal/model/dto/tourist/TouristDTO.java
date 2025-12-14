package com.desertakal.desertakal.model.dto.tourist;

import com.desertakal.desertakal.model.dto.language.LanguageDTO;
import com.desertakal.desertakal.model.dto.user.UserDTO;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TouristDTO extends UserDTO {
    private String avatarUrl;
    private String nationality;
    private String language;
}
