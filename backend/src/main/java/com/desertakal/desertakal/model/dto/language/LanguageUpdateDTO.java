package com.desertakal.desertakal.model.dto.language;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class LanguageUpdateDTO {
    @Size(min = 4, max = 50, message = "Language name must be between 4 and 50 characters")
    private String name;

    @Pattern(regexp = "^[a-z]{2,3}$", message = "Language code must be 2 or 3 lowercase letters")
    private String code;
}
