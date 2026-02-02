package com.desertakal.desertakal.repository;

import com.desertakal.desertakal.model.entity.Tour;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TourRepository extends JpaRepository<@NonNull Tour, @NonNull UUID>, JpaSpecificationExecutor<@NonNull Tour> {
    Optional<@NonNull Tour> findByUuid(@NonNull UUID uuid);

    List<@NonNull Tour> findTop5ByOrderByRatingDesc();
}
