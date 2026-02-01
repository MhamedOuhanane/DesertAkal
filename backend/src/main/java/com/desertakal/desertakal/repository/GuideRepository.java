package com.desertakal.desertakal.repository;

import com.desertakal.desertakal.model.entity.Guide;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface GuideRepository extends JpaRepository<@NonNull Guide, @NonNull UUID>, JpaSpecificationExecutor<@NonNull Guide> {
    Optional<@NonNull Guide> findByUuid(@NonNull UUID uuid);
}
