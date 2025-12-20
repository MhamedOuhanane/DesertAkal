package com.desertakal.desertakal.model.dto.article;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleUpdateDTO {
    private String content;
}
