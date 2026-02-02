package com.desertakal.desertakal.repository;

import com.desertakal.desertakal.model.entity.Guide;
import com.desertakal.desertakal.model.entity.Tour;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GuideRepository extends JpaRepository<@NonNull Guide, @NonNull UUID>, JpaSpecificationExecutor<@NonNull Guide> {
    Optional<@NonNull Guide> findByUuid(@NonNull UUID uuid);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
    List<@NonNull Guide> findTop5ByOrderByRatingDesc();
}
