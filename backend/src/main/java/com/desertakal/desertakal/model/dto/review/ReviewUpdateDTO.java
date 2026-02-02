package com.desertakal.desertakal.model.dto.review;

import com.desertakal.desertakal.model.enums.ReviewableType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewUpdateDTO {
    @DecimalMin(value = "0.0", inclusive = true, message = "Rating must be at least 0")
    @DecimalMax(value = "5.0", inclusive = true, message = "Rating must be at most 5")
    private BigDecimal rating;

    @Size(min = 20, max = 1000, message = "Comment must be between 20 and 1000 characters")
    private String comment;

}
