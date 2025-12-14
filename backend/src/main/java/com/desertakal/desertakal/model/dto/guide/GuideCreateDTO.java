package com.desertakal.desertakal.model.dto.guide;

import com.desertakal.desertakal.model.dto.auth.RegisterDTO;
import com.desertakal.desertakal.model.dto.language.LanguageDTO;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class GuideCreateDTO extends RegisterDTO {
    private Integer experienceYears;
    private String phone;
    private String photo;
    private List<LanguageDTO> languages;
}
