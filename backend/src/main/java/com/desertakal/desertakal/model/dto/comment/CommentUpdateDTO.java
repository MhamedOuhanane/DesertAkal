package com.desertakal.desertakal.model.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentUpdateDTO {
    @NotBlank(message = "Content is required")
    @Size(min = 1, max = 1000,
            message = "Content must be between 1 and 1000 characters")
    private String content;
}
