package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.exception.custom.ResourceNotFoundException;
import com.desertakal.desertakal.model.dto.reservation.ReservationCreateDTO;
import com.desertakal.desertakal.model.dto.reservation.ReservationFindDTO;
import com.desertakal.desertakal.model.dto.reservation.ReservationUpdateDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.entity.Guide;
import com.desertakal.desertakal.model.entity.Reservation;
import com.desertakal.desertakal.model.entity.Tour;
import com.desertakal.desertakal.model.entity.Tourist;
import com.desertakal.desertakal.model.enums.ReservationStatus;
import com.desertakal.desertakal.model.mapper.ReservationMapper;
import com.desertakal.desertakal.repository.GuideRepository;
import com.desertakal.desertakal.repository.ReservationRepository;
import com.desertakal.desertakal.repository.TourRepository;
import com.desertakal.desertakal.repository.TouristRepository;
import com.desertakal.desertakal.service.interfaces.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReservationServiceImpl implements ReservationService {
    private final ReservationRepository repository;
    private final ReservationMapper mapper;
    private final TouristRepository touristRepository;
    private final GuideRepository guideRepository;
    private final TourRepository tourRepository;

    @Override
    public ReservationFindDTO create(@NonNull ReservationCreateDTO dto, @NonNull UUID touristUuid) {
        log.info("Starting reservation creation process for Tourist: {} on Tour: {}", touristUuid, dto.getTourUuid());

        Tourist tourist = touristRepository.findByUuid(touristUuid)
                .orElseThrow(() -> {
                    log.error("Creation failed: Tourist with UUID {} not found", touristUuid);
                    return new ResourceNotFoundException("Tourist", "identifier", touristUuid.toString());
                });

        Guide guide = guideRepository.findByUuid(dto.getGuideUuid())
                .orElseThrow(() -> {
                    log.error("Creation failed: Guide with UUID {} not found", dto.getGuideUuid());
                    return new ResourceNotFoundException("Guide", "identifier", dto.getGuideUuid().toString());
                });

        Tour tour = tourRepository.findByUuid(dto.getTourUuid())
                .orElseThrow(() -> {
                    log.error("Creation failed: Tour with UUID {} not found", dto.getTourUuid());
                    return new ResourceNotFoundException("Tour", "identifier", dto.getTourUuid().toString());
                });

        log.debug("Mapping DTO to Entity and setting relationships for reservation");

        Reservation reservation = mapper.toEntity(dto);
        reservation.setTourist(tourist);
        reservation.setTour(tour);
        reservation.setGuide(guide);

        try {
            Reservation newReservation = repository.save(reservation);
            log.info("Reservation successfully created with UUID: {} for Tourist: {}", newReservation.getUuid(), touristUuid);
            return mapper.toFindDto(newReservation);
        } catch (Exception e) {
            log.error("Database error while saving reservation: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public ReservationFindDTO update(@NonNull UUID reservationUuid, @NonNull ReservationUpdateDTO dto) {
        return null;
    }

    @Override
    public void cancel(@NonNull UUID reservationUuid) {

    }

    @Override
    public ReservationFindDTO get(@NonNull UUID reservationUuid) {
        return null;
    }

    @Override
    public PaginationDTO getAll(String tour, String guide, String tourist, ReservationStatus status, LocalDateTime date, @NonNull Pageable pageable) {
        return null;
    }

    @Override
    public PaginationDTO getByTourist(@NonNull UUID touristUuid, String tour, String guide, ReservationStatus status, LocalDateTime date, @NonNull Pageable pageable) {
        return null;
    }

    @Override
    public PaginationDTO getByGuide(@NonNull UUID guideUuid, String tour, String tourist, ReservationStatus status, LocalDateTime date, @NonNull Pageable pageable) {
        return null;
    }

    @Override
    public void delete(@NonNull UUID reservationUuid) {

    }
}
