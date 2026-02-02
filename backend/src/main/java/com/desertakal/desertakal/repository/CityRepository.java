package com.desertakal.desertakal.repository;

import com.desertakal.desertakal.model.entity.City;
import com.desertakal.desertakal.model.entity.Permission;
import com.desertakal.desertakal.model.entity.Tour;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CityRepository extends JpaRepository<@NonNull City, @NonNull UUID>, JpaSpecificationExecutor<@NonNull City> {
    Optional<@NonNull City> findByUuid(@NonNull UUID uuid);

    List<@NonNull City> findByCityToursTour(@NonNull Tour tour);
    List<@NonNull City> findDistinctByUuidIn(List<@NonNull UUID> uuids);

    boolean existsByName(@NonNull String name);
}
