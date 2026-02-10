package com.desertakal.desertakal.repository;

import com.desertakal.desertakal.model.entity.Guide;
import com.desertakal.desertakal.model.entity.Reservation;
import com.desertakal.desertakal.model.entity.Tourist;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<@NonNull Reservation, @NonNull UUID>, JpaSpecificationExecutor<@NonNull Reservation> {
    Optional<@NonNull Reservation> findByUuid(@NonNull UUID uuid);

    Page<@NonNull Reservation> findAllByTourist(Tourist tourist, Specification<@NonNull Reservation> spec, Pageable pageable);
    Page<@NonNull Reservation> findAllByGuide(Guide guide, Specification<@NonNull Reservation> spec, Pageable pageable);
}
