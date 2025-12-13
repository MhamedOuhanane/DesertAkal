package com.desertakal.desertakal.model.dto.language;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageUpdateDTO {
    private String name;
    private String code;
}
