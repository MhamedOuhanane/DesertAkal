package com.desertakal.desertakal.model.dto.review;

import com.desertakal.desertakal.model.enums.ReviewableType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreateDTO {
    @NotNull(message = "Rating is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Rating must be at least 0")
    @DecimalMax(value = "5.0", inclusive = true, message = "Rating must be at most 5")
    private BigDecimal rating;

    @NotNull(message = "Comment is required")
    @Size(max = 1000, message = "Comment must be at most 1000 characters")
    private String comment;

    @NotNull(message = "Reviewable UUID is required")
    private Long reviewableUuid;

    @NotNull(message = "Reviewable type is required")
    private ReviewableType reviewableType;
}
