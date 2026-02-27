package com.desertakal.desertakal.service.interfaces;

import com.desertakal.desertakal.model.dto.reservation.ReservationCreateDTO;
import com.desertakal.desertakal.model.dto.reservation.ReservationFindDTO;
import com.desertakal.desertakal.model.dto.reservation.ReservationUpdateDTO;
import com.desertakal.desertakal.model.dto.reservation.ReservationVerificationDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.enums.ReservationStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ReservationService {
    ReservationFindDTO create(@NonNull ReservationCreateDTO dto, @NonNull UUID touristUuid);
    ReservationFindDTO update(@NonNull UUID reservationUuid, @NonNull ReservationUpdateDTO dto, @NonNull UUID currentUserUuid);
    void cancel(@NonNull UUID reservationUuid, @NonNull UUID currentUserUuid, boolean isAdmin);
    ReservationFindDTO get(@NonNull UUID reservationUuid, @NonNull UUID currentUserUuid, boolean isAdmin);
    ReservationFindDTO get(@NonNull String reference, @NonNull UUID currentUserUuid, boolean isAdmin);
    PaginationDTO getAll(String tour, String guide, String tourist, ReservationStatus status, LocalDateTime startDate, LocalDateTime endDate, @NonNull Pageable pageable);
    PaginationDTO getByTourist(@NonNull UUID touristUuid, String tour, String guide, ReservationStatus status, LocalDateTime startDate, LocalDateTime endDate, @NonNull Pageable pageable);
    PaginationDTO getByGuide(@NonNull UUID guideUuid, String tour, String tourist, ReservationStatus status, LocalDateTime startDate, LocalDateTime endDate, @NonNull Pageable pageable);
    void delete(@NonNull UUID reservationUuid);
    byte[] getReservationPdfContent(@NonNull UUID reservationUuid, @NonNull UUID currentUserUuid, boolean isAdmin);
    public ReservationVerificationDTO verifyReservation(@NonNull UUID uuid);
}
