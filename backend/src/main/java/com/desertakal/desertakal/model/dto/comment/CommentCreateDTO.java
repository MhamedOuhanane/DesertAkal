package com.desertakal.desertakal.model.dto.comment;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateDTO {
    private String content;
}
