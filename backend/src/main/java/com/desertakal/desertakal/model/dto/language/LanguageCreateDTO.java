package com.desertakal.desertakal.model.dto.language;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageCreateDTO {
    private String name;
    private String code;
}
