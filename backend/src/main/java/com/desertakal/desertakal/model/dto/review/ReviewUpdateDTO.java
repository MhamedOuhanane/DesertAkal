package com.desertakal.desertakal.model.dto.review;

import com.desertakal.desertakal.model.enums.ReviewableType;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewUpdateDTO {
    private BigDecimal rating;
    private String comment;
}
