package com.desertakal.desertakal.repository;

import com.desertakal.desertakal.model.entity.Tourist;
import com.desertakal.desertakal.model.enums.ReservationStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TouristRepository extends JpaRepository<@NonNull Tourist, @NonNull UUID>, JpaSpecificationExecutor<@NonNull Tourist> {
    Optional<@NonNull Tourist> findByUuid(@NonNull UUID uuid);

    @Query("select count(r) > 0 from Reservation r where r.tourist = :tourist")
    boolean hasAnyReservation(@Param("tourist") Tourist tourist);

    @Query("select count(r) > 0 from Reservation r where r.tourist = :tourist and r.status in :statuses")
    boolean hasReservationsWithStatuses(@Param("tourist") Tourist tourist, @Param("statuses") List<ReservationStatus> statuses);
}
