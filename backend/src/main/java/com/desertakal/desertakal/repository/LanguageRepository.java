package com.desertakal.desertakal.repository;

import com.desertakal.desertakal.model.entity.Language;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LanguageRepository extends JpaRepository<@NonNull Language, @NonNull UUID>, JpaSpecificationExecutor<@NonNull Language> {
    Optional<Language> findByUuid(UUID uuid);
    Optional<Language> findByName(String name);

    List<@NonNull Language> findDistinctByUuidIn(List<@NonNull UUID> uuids);

    boolean existsByName(String name);
}
