package com.desertakal.desertakal.model.dto.article;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDTO {
    private UUID uuid;
    private String content;
    private String coverImage;
    private Integer commentCount;
    private Integer reactionCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID userUuid;
    private String userName;
    private String userPhoto;
}
