package com.desertakal.desertakal.repository;

import com.desertakal.desertakal.model.entity.Guide;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GuideRepository extends JpaRepository<@NonNull Guide, @NonNull UUID>, JpaSpecificationExecutor<@NonNull Guide> {
    Optional<@NonNull Guide> findByUuid(@NonNull UUID uuid);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
    List<@NonNull Guide> findTop5ByOrderByRatingDesc();

    @Query("""
        SELECT DISTINCT g FROM Guide g
            JOIN g.languages l
                WHERE LOWER(l.name) LIKE LOWER(CONCAT('%', :language, '%'))
                    AND NOT EXISTS (
                        SELECT r FROM Reservation r
                            WHERE r.guide = g
                                AND r.status = 'CONFIRMED'
                                    AND r.startDate < :endDate 
                                        AND r.endDate > :startDate
                    )
    """)
    List<Guide> findAvailableGuides(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("language") String language
    );

    @Query("""
        select COUNT(r) = 0 from Reservation r
            where r.guide.uuid = :guide
                and r.status in (
                        com.desertakal.desertakal.model.enums.ReservationStatus.CONFIRMED,
                        com.desertakal.desertakal.model.enums.ReservationStatus.PENDING
                    )
                        and r.startDate < :endDate
                            and r.endDate > :startDate
    """)
    boolean isGuideAvailable(
            @Param("guide") Guide guide,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
