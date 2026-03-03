package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.exception.custom.BusinessRuleException;
import com.desertakal.desertakal.exception.custom.ResourceNotFoundException;
import com.desertakal.desertakal.exception.custom.UnauthorizedActionException;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.dto.review.ReviewCreateDTO;
import com.desertakal.desertakal.model.dto.review.ReviewDTO;
import com.desertakal.desertakal.model.dto.review.ReviewUpdateDTO;
import com.desertakal.desertakal.model.entity.Review;
import com.desertakal.desertakal.model.entity.Tourist;
import com.desertakal.desertakal.model.enums.ReviewableType;
import com.desertakal.desertakal.model.mapper.ReviewMapper;
import com.desertakal.desertakal.repository.ReviewRepository;
import com.desertakal.desertakal.repository.TouristRepository;
import com.desertakal.desertakal.service.interfaces.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository repository;
    private final ReviewMapper mapper;
    private ReviewableResolver resolver;
    private final TouristRepository touristRepository;

    @Override
    public ReviewDTO create(@NonNull ReviewCreateDTO dto, @NonNull UUID touristUuid) {
        log.info("Creating review for {} {} by tourist {}",
                dto.getReviewableType(), dto.getReviewableUuid(), touristUuid);

        Tourist tourist = findTourist(touristUuid);

        resolver.resolve(dto.getReviewableUuid(), dto.getReviewableType());

        if (repository.existsByTouristUuidAndReviewableUuidAndReviewableType(
                        tourist.getUuid(),
                        dto.getReviewableUuid(),
                        dto.getReviewableType())
        ) {
            throw new BusinessRuleException(
                    String.format("You have already reviewed this %s. ",dto.getReviewableType().name().toLowerCase())
            );
        }

        Review review = mapper.toEntity(dto);
        review.setTourist(tourist);
        repository.save(review);

        resolver.recalculateRating(dto.getReviewableUuid(), dto.getReviewableType());

        log.info("Review {} created for {} {}",
                review.getUuid(), dto.getReviewableType(), dto.getReviewableUuid());

        ReviewDTO reviewDTO = mapper.toDto(review);
        reviewDTO.setReviewableName(resolver.getDisplayName(dto.getReviewableUuid(), dto.getReviewableType()));

        return reviewDTO;
    }

    @Override
    public ReviewDTO update(@NonNull UUID reviewUuid, @NonNull ReviewUpdateDTO dto, @NonNull UUID touristUuid) {
        return null;
    }

    @Override
    public void delete(@NonNull UUID reviewUuid, @NonNull UUID currentUserUuid, boolean isAdmin) {

    }

    @Override
    public ReviewDTO get(@NonNull UUID reviewUuid) {
        return null;
    }

    @Override
    public PaginationDTO getByReviewable(@NonNull UUID reviewableUuid, @NonNull ReviewableType type, BigDecimal minRating, @NonNull Pageable pageable) {
        return null;
    }

    @Override
    public PaginationDTO getByTourist(@NonNull UUID touristUuid, BigDecimal minRating, @NonNull Pageable pageable) {
        return null;
    }

    @Override
    public PaginationDTO getAll(ReviewableType type, BigDecimal minRating, @NonNull Pageable pageable) {
        return null;
    }

    private Review findReview(UUID uuid) {
        return repository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("Review not found: {}", uuid);
                    return new ResourceNotFoundException(
                            "Review", "uuid", uuid.toString());
                });
    }

    private Tourist findTourist(UUID uuid) {
        return touristRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("Tourist not found: {}", uuid);
                    return new ResourceNotFoundException(
                            "Tourist", "uuid", uuid.toString());
                });
    }

    private void validateOwnership(Review review, UUID touristUuid, boolean isAdmin) {
        if (!review.getTourist().getUuid().equals(touristUuid) && !isAdmin) {
            throw new UnauthorizedActionException("You can only modify or delete your own reviews.");
        }
    }

    private PaginationDTO buildPaginationDTO(Page<@NonNull Review> page) {
        return PaginationDTO.builder()
                .content(mapper.toDtos(page.getContent()))
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }
}
