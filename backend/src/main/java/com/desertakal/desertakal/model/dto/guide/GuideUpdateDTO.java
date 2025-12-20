package com.desertakal.desertakal.model.dto.guide;

import com.desertakal.desertakal.model.dto.auth.RegisterDTO;
import com.desertakal.desertakal.model.dto.language.LanguageDTO;
import com.desertakal.desertakal.model.dto.user.UserUpdateDTO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    @Min(value = 0, message = "Experience years cannot be negative")
    @Max(value = 50, message = "Experience years value is unrealistic")
    private Integer experienceYears;
    private List<UUID> languageUsUuids;
}
