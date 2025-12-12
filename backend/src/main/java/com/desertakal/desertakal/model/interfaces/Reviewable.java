package com.desertakal.desertakal.model.interfaces;

import com.desertakal.desertakal.model.enums.ReviewableType;

import java.math.BigDecimal;

public interface Reviewable {
    Long getId();
    ReviewableType getReviewableType();
    String getDisplayName();
    default void updateAverageRating(BigDecimal newAverage) {}
    default void incrementReviewCount() {}
    default void decrementReviewCount() {}
}
