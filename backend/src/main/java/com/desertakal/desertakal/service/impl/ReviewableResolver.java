package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.exception.custom.ResourceNotFoundException;
import com.desertakal.desertakal.model.enums.ReviewableType;
import com.desertakal.desertakal.model.interfaces.Reviewable;
import com.desertakal.desertakal.repository.GuideRepository;
import com.desertakal.desertakal.repository.ReviewRepository;
import com.desertakal.desertakal.repository.TourRepository;
import com.desertakal.desertakal.repository.TouristRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewableResolver {
    private final ReviewRepository repository;
    private final TourRepository tourRepository;
    private final GuideRepository guideRepository;

    public Reviewable resolve(UUID uuid, ReviewableType type) {
        return switch (type) {
            case TOUR -> tourRepository.findByUuid(uuid)
                    .orElseThrow(() -> {
                        log.error("Tour not found: {}", uuid);
                        return new ResourceNotFoundException(
                                "Tour", "uuid", uuid.toString());
                    });

            case GUIDE -> guideRepository.findByUuid(uuid)
                    .orElseThrow(() -> {
                        log.error("Guide not found: {}", uuid);
                        return new ResourceNotFoundException(
                                "Guide", "uuid", uuid.toString());
                    });
        };
    }

    @Transactional
    public void recalculateRating(@NonNull UUID reviewableUuid, @NonNull ReviewableType type) {
        Reviewable reviewable = resolve(reviewableUuid, type);

        BigDecimal avgRating = repository.calculateAverageRating(reviewableUuid, type);
        long count = repository.countByReviewable(reviewableUuid, type);

        reviewable.updateAverageRating(avgRating.setScale(2, RoundingMode.HALF_UP));
        reviewable.setReviewCount((int) count);

        log.info("{} {} rating updated: {} ({} reviews)", type, reviewableUuid, avgRating, count);
    }

    public String getDisplayName(UUID uuid, ReviewableType type) {
        try {
            return resolve(uuid, type).getDisplayName();
        } catch (Exception e) {
            return null;
        }
    }
}
