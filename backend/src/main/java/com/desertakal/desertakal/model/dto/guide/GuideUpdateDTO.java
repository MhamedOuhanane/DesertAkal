package com.desertakal.desertakal.model.dto.guide;

import com.desertakal.desertakal.model.dto.auth.RegisterDTO;
import com.desertakal.desertakal.model.dto.language.LanguageDTO;
import com.desertakal.desertakal.model.dto.user.UserUpdateDTO;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class GuideUpdateDTO extends UserUpdateDTO {
    private Integer experienceYears;
    private List<UUID> languageUsUuids;
}
