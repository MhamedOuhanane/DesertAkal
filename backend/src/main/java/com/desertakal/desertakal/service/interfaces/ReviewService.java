package com.desertakal.desertakal.service.interfaces;

import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.dto.review.ReviewCreateDTO;
import com.desertakal.desertakal.model.dto.review.ReviewDTO;
import com.desertakal.desertakal.model.dto.review.ReviewUpdateDTO;
import com.desertakal.desertakal.model.enums.ReviewableType;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface ReviewService {

    ReviewDTO create(@NonNull ReviewCreateDTO dto, @NonNull UUID touristUuid);

    ReviewDTO update(@NonNull UUID reviewUuid, @NonNull ReviewUpdateDTO dto, @NonNull UUID touristUuid);

    void delete(@NonNull UUID reviewUuid, @NonNull UUID currentUserUuid, boolean isAdmin);

    ReviewDTO get(@NonNull UUID reviewUuid);

    PaginationDTO getByReviewable(@NonNull UUID reviewableUuid, @NonNull ReviewableType type, BigDecimal minRating, @NonNull Pageable pageable);

    PaginationDTO getByTourist(@NonNull UUID touristUuid, BigDecimal minRating, @NonNull Pageable pageable);

    PaginationDTO getAll(ReviewableType type, BigDecimal minRating, @NonNull Pageable pageable);
}

