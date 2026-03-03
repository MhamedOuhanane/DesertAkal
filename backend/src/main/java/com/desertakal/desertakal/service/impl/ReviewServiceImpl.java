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
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository repository;
    private final ReviewMapper mapper;
    private final ReviewableResolver resolver;
    private final TouristRepository touristRepository;

    @Override
    @Transactional
    public ReviewDTO create(@NonNull ReviewCreateDTO dto, @NonNull UUID touristUuid) {
        log.info("Creating review for {} {} by tourist {}",
                dto.getReviewableType(), dto.getReviewableUuid(), touristUuid);

        Tourist tourist = findTourist(touristUuid);

        resolver.resolve(dto.getReviewableUuid(), dto.getReviewableType());

        if (repository.existsByTourist_UuidAndReviewableUuidAndReviewableType(
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
        review.setReviewableName(resolver.getDisplayName(dto.getReviewableUuid(), dto.getReviewableType()));
        repository.save(review);

        resolver.recalculateRating(dto.getReviewableUuid(), dto.getReviewableType());

        log.info("Review {} created for {} {}",
                review.getUuid(), dto.getReviewableType(), dto.getReviewableUuid());

        return mapper.toDto(review);
    }

    @Override
    @Transactional
    public ReviewDTO update(@NonNull UUID reviewUuid, @NonNull ReviewUpdateDTO dto, @NonNull UUID touristUuid) {

        log.info("Updating review {} by tourist {}",
                reviewUuid, touristUuid);

        Review review = findReview(reviewUuid);

        validateOwnership(review, touristUuid);

        mapper.updateEntityFromDto(dto, review);
        review = repository.save(review);

        resolver.recalculateRating(review.getReviewableUuid(), review.getReviewableType());

        log.info("Review {} updated", reviewUuid);

        return mapper.toDto(review);
    }

    @Override
    @Transactional
    public void delete(@NonNull UUID reviewUuid, @NonNull UUID currentUserUuid, boolean isAdmin) {

        log.info("Deleting review {} by user {} (admin: {})",
                reviewUuid, currentUserUuid, isAdmin);

        Review review = findReview(reviewUuid);

        if (!isAdmin) {
            validateOwnership(review, currentUserUuid);
        }

        UUID reviewableUuid = review.getReviewableUuid();
        ReviewableType reviewableType = review.getReviewableType();

        repository.delete(review);

        resolver.recalculateRating(reviewableUuid, reviewableType);

        log.info("Review {} deleted. Rating recalculated for {} {}",
                reviewUuid, reviewableType, reviewableUuid);
    }

    @Override
    public ReviewDTO get(@NonNull UUID reviewUuid) {
        log.info("Fetching review: {}", reviewUuid);

        Review review = findReview(reviewUuid);

        return mapper.toDto(review);
    }

    @Override
    public PaginationDTO getByReviewable(@NonNull UUID reviewableUuid, @NonNull ReviewableType type, BigDecimal minRating, @NonNull Pageable pageable) {
        log.info("Fetching reviews for {} {} [Page: {}]",
                type, reviewableUuid, pageable.getPageNumber());

        Specification<@NonNull Review> spec = getSpecification(null, reviewableUuid, type, minRating);

        Page<@NonNull Review> page = repository.findAll(spec, pageable);

        return buildPaginationDTO(page);
    }

    @Override
    public PaginationDTO getByTourist(@NonNull UUID touristUuid, BigDecimal minRating, @NonNull Pageable pageable) {
        log.info("Fetching reviews by tourist {} (minRating: {})", touristUuid, minRating);
        Specification<@NonNull Review> spec = getSpecification(touristUuid, null, null, minRating);
        return buildPaginationDTO(repository.findAll(spec, pageable));
    }

    @Override
    public PaginationDTO getAll(ReviewableType type, BigDecimal minRating, @NonNull Pageable pageable) {
        log.info("Admin fetching all reviews [Type: {}, minRating: {}]", type, minRating);
        Specification<@NonNull Review> spec = getSpecification(null, null, type, minRating);
        return buildPaginationDTO(repository.findAll(spec, pageable));
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

    private void validateOwnership(Review review, UUID touristUuid) {
        if (!review.getTourist().getUuid().equals(touristUuid)) {
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

    private Specification<@NonNull Review> getSpecification(UUID touristUuid, UUID reviewableUuid, ReviewableType type, BigDecimal minRating) {
        return (root, query, cb) -> {

            log.debug("Building Review Specification [touristUuid: {}, reviewableUuid: {}, type: {}, minRating: {}]",
                    touristUuid, reviewableUuid, type, minRating);

            List<Predicate> predicates = new ArrayList<>();

            if (touristUuid != null) {
                predicates.add(cb.equal(root.get("tourist").get("uuid"), touristUuid));
                log.debug("Filter applied: tourist.uuid = '{}'", touristUuid);
            }

            if (reviewableUuid != null) {
                predicates.add(cb.equal(root.get("reviewableUuid"), reviewableUuid));
                log.debug("Filter applied: reviewableUuid = '{}'", reviewableUuid);
            }

            if (type != null) {
                predicates.add(cb.equal(root.get("reviewableType"), type));
                log.debug("Filter applied: reviewableType = '{}'", type);
            }

            if (minRating != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("rating"), minRating));
                log.debug("Filter applied: rating >= '{}'", minRating);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
