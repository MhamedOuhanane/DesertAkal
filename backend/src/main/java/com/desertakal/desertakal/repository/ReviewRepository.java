package com.desertakal.desertakal.repository;

import com.desertakal.desertakal.model.entity.Review;
import com.desertakal.desertakal.model.enums.ReviewableType;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<@NonNull Review, @NonNull UUID>, JpaSpecificationExecutor<@NonNull Review> {
    
    Optional<@NonNull Review> findByUuid(@NonNull UUID uuid);

    boolean existsByTouristUuidAndReviewableUuidAndReviewableType(UUID touristUuid, UUID reviewableUuid, ReviewableType reviewableType);

    @Query("""
        select coalesce(avg(r.rating), 0) from Review r
            where r.reviewableUuid = :reviewableUuid
                and r.reviewableType = :reviewableType
    """)
    BigDecimal calculateAverageRating(@Param("reviewableUuid") UUID reviewableUuid, @Param("reviewableType") ReviewableType reviewableType);

    @Query("""
        select count(r) from Review r
            where r.reviewableUuid = :reviewableUuid
                and r.reviewableType = :reviewableType
    """)
    long countByReviewable(@Param("reviewableUuid") UUID reviewableUuid, @Param("reviewableType") ReviewableType reviewableType);
}
