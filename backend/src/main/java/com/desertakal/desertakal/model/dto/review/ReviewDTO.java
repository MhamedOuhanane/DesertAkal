package com.desertakal.desertakal.model.dto.review;

import com.desertakal.desertakal.model.enums.ReviewableType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private UUID uuid;
    private BigDecimal rating;
    private String comment;
    private Long reviewableUuid;
    private ReviewableType reviewableType;
    private UUID touristUuid;
    private String touristName;
    private String touristPhoto;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
