package com.desertakal.desertakal.repository;

import com.desertakal.desertakal.model.entity.Guide;
import com.desertakal.desertakal.model.entity.Tour;
import com.desertakal.desertakal.model.entity.Tourist;
import io.lettuce.core.dynamic.annotation.Param;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TourRepository extends JpaRepository<@NonNull Tour, @NonNull UUID>, JpaSpecificationExecutor<@NonNull Tour> {
    Optional<@NonNull Tour> findByUuid(@NonNull UUID uuid);

    List<@NonNull Tour> findTop5ByOrderByRatingDesc();

    @Query("SELECT t FROM Tour t JOIN t.reservations r WHERE r.tourist = :tourist")
    Page<@NonNull Tour> findAllByTourist(@Param("tourist") Tourist tourist, Pageable pageable);

    @Query("SELECT t FROM Tour t JOIN t.reservations r WHERE r.guide = :guide")
    Page<@NonNull Tour> findAllByGuide(@Param("guide") Guide guide, Pageable pageable);

    @Query("SELECT AVG(t.rating) FROM Tour t")
    Double getAverageRating();

    boolean existsByTitle(String title);
}
