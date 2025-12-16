package com.desertakal.desertakal.model.dto.article;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleCreateDTO {
    private String content;
    private UUID userUuid;
}
