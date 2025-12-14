package com.desertakal.desertakal.model.dto.guide;

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
public class GuideDTO extends UserDTO {
    private Integer experienceYears;
    private BigDecimal rating;
    private Integer reviewCount;
    private List<LanguageDTO> languages;
}
