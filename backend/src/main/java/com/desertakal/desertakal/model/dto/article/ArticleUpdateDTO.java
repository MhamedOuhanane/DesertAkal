package com.desertakal.desertakal.model.dto.article;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleUpdateDTO {

    @Size(min = 10, max = 5000, message = "Content must be between 10 and 5000 characters")
    private String content;
}
