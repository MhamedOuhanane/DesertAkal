package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.exception.custom.*;
import com.desertakal.desertakal.model.dto.reservation.ReservationCreateDTO;
import com.desertakal.desertakal.model.dto.reservation.ReservationFindDTO;
import com.desertakal.desertakal.model.dto.reservation.ReservationUpdateDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.entity.*;
import com.desertakal.desertakal.model.enums.ReservationStatus;
import com.desertakal.desertakal.model.mapper.ReservationMapper;
import com.desertakal.desertakal.repository.*;
import com.desertakal.desertakal.service.interfaces.NotificationService;
import com.desertakal.desertakal.service.interfaces.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReservationFindDTO create(@NonNull ReservationCreateDTO dto, @NonNull UUID touristUuid) {
        log.info("Starting reservation creation process for Tourist: {} on Tour: {}", touristUuid, dto.getTourUuid());

        Tourist tourist = touristRepository.findByUuid(touristUuid)
                .orElseThrow(() -> {
                    log.error("Creation failed: Tourist with UUID {} not found", touristUuid);
                    return new ResourceNotFoundException("Tourist", "identifier", touristUuid.toString());
                });

        boolean hasActiveReservation = touristRepository.hasReservationsWithStatuses(
                tourist,
                List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED)
        );

        if (hasActiveReservation) {
            log.warn("Creation rejected: Tourist {} already has a pending or confirmed reservation", touristUuid);
            throw new BusinessRuleException(
                    "You already have an active reservation (Pending or Confirmed). You cannot create a new one until the current one is completed or cancelled."
            );
        }

        LocalDateTime minimumAllowedDate = LocalDateTime.now().plusWeeks(1);

        if (dto.getStartDate().isBefore(minimumAllowedDate)) {
            log.warn("Creation rejected: Start date {} is too close. Minimum 7 days lead time required.", dto.getStartDate());
            throw new BadRequestException(
                    "Reservations must be made at least one week in advance to allow for preparation."
            );
        }

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

        boolean isGuideAvailable = guideRepository.isGuideAvailable(
                guide,
                dto.getStartDate(),
                dto.getStartDate().plusDays(tour.getDurationDays())
        );

        if (!isGuideAvailable) {
            log.warn("Assignment failed: Guide {} is busy during requested dates", dto.getGuideUuid());
            throw new GuideNotAvailableException(
                    String.format("Guide %s is already assigned to another tour or has a pending request during this period", guide.getFullName())
            );
        }


        log.debug("Mapping DTO to Entity and setting relationships for reservation");

        Reservation reservation = mapper.toEntity(dto);
        reservation.setTourist(tourist);
        reservation.setTour(tour);
        reservation.setGuide(guide);
        reservation.setPdfUrl("https://desertakal.com/res/pdf/" + UUID.randomUUID());
        reservation.setQrCode("QR_" + UUID.randomUUID());

        try {
            Reservation newReservation = repository.save(reservation);
            log.info("Reservation successfully created with UUID: {} for Tourist: {}", newReservation.getUuid(), touristUuid);
            sendReservationNotifications(tour, tourist, guide);
            return mapper.toFindDto(newReservation);
        } catch (Exception e) {
            log.error("Database error while saving reservation: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public ReservationFindDTO update(@NonNull UUID reservationUuid, @NonNull ReservationUpdateDTO dto, @NonNull UUID currentUserUuid) {
        log.info("Starting update for Reservation UUID: {}", reservationUuid);

        Reservation reservation = repository.findByUuid(reservationUuid)
                .orElseThrow(() -> {
                    log.error("Update failed: Reservation {} not found", reservationUuid);
                    return new ResourceNotFoundException("Reservation", "identifier", reservationUuid.toString());
                });

        if (!reservation.getTourist().getUuid().equals(currentUserUuid)) {
            log.error("Security violation: User {} tried to update reservation belonging to user {}",
                    currentUserUuid, reservation.getTourist().getUuid());
            throw new UnauthorizedActionException("You are not the owner of this reservation.");
        }

        if (!reservation.getStatus().equals(ReservationStatus.PENDING)) {
            log.warn("Update rejected: Reservation {} is in status {}", reservationUuid, reservation.getStatus());
            throw new ReservationStatusException("update", reservation.getStatus());
        }

        boolean isNewStartDate = dto.getStartDate() != null && !dto.getStartDate().equals(reservation.getStartDate());
        boolean isNewGuide = dto.getGuideUuid() != null && !dto.getGuideUuid().equals(reservation.getGuide().getUuid());

        if (isNewStartDate) {
            if (dto.getStartDate().isBefore(reservation.getStartDate())) {
                log.warn("Update rejected: New start date {} is before current start date {}",
                        dto.getStartDate(), reservation.getStartDate());
                throw new BusinessRuleException("You can only postpone your tour. The new start date must be after the original start date.");
            }

            if (dto.getStartDate().isBefore(LocalDateTime.now().plusDays(3))) {
                log.warn("Update rejected: New start date {} is too close to current date", dto.getStartDate());
                throw new BadRequestException("The new start date must be at least 3 days from today.");
            }
        }


        if (reservation.getStartDate().isBefore(LocalDateTime.now().plusDays(3))) {
            log.warn("Update rejected: Current start date {} is within 3-day limit", reservation.getStartDate());
            throw new BusinessRuleException("Cannot update reservation: The tour starts in less than 3 days.");
        }


        Guide oldGuide = reservation.getGuide();
        Guide currentGuide = oldGuide;

        if (isNewGuide || isNewStartDate) {
            if (isNewGuide) {
                currentGuide = guideRepository.findByUuid(dto.getGuideUuid())
                        .orElseThrow(() -> new ResourceNotFoundException("Guide", "identifier", dto.getGuideUuid().toString()));
            }

            LocalDateTime targetStartDate = isNewStartDate ? dto.getStartDate() : reservation.getStartDate();

            boolean isGuideAvailable = guideRepository.isGuideAvailable(
                    currentGuide,
                    targetStartDate,
                    targetStartDate.plusDays(reservation.getTour().getDurationDays())
            );

            if (!isGuideAvailable) {
                log.error("Update failed: Guide {} not available for period {} to {}",
                        currentGuide.getUuid(), targetStartDate, targetStartDate.plusDays(reservation.getTour().getDurationDays()));
                throw new GuideNotAvailableException(
                        String.format("Guide %s is already assigned or has a pending request during this period", currentGuide.getFullName())
                );
            }
        }

        mapper.updateEntityFromDto(dto, reservation);
        reservation.setGuide(currentGuide);

        if (isNewStartDate) {
            reservation.setEndDate(dto.getStartDate().plusDays(reservation.getTour().getDurationDays()));
        }

        repository.save(reservation);
        log.info("Reservation {} successfully updated", reservationUuid);

        sendUpdateNotifications(reservation, oldGuide, currentGuide, isNewGuide, isNewStartDate);

        return mapper.toFindDto(reservation);
    }

    @Override
    @Transactional
    public void cancel(@NonNull UUID reservationUuid, @NonNull UUID currentUserUuid, boolean isAdmin) {
        log.info("Starting cancellation process for Reservation: {} by User: {}", reservationUuid, currentUserUuid);

        Reservation reservation = repository.findByUuid(reservationUuid)
                .orElseThrow(() -> {
                    log.error("Cancellation failed: Reservation not found with UUID: {}", reservationUuid);
                    return new ResourceNotFoundException("Reservation", "identifier", reservationUuid.toString());
                });

        boolean isOwner = reservation.getTourist().getUuid().equals(currentUserUuid);

        if (!isOwner && !isAdmin) {
            log.error("Unauthorized Access: User {} attempted to cancel reservation {} owned by {}",
                    currentUserUuid, reservationUuid, reservation.getTourist().getUuid());
            throw new UnauthorizedActionException("Access denied: You are not authorized to cancel this reservation.");
        }

        if (reservation.getStatus().equals(ReservationStatus.CANCELLED)) {
            log.warn("Redundant Request: Reservation {} is already cancelled", reservationUuid);
            throw new ReservationStatusException("cancel", reservation.getStatus());
        }

        if (reservation.getStatus().equals(ReservationStatus.COMPLETED)) {
            log.error("Invalid Action: Cannot cancel a completed reservation: {}", reservationUuid);
            throw new BusinessRuleException("Cannot cancel a tour that has already been completed.");
        }

        if (reservation.getStatus().equals(ReservationStatus.REJECTED)) {
            log.warn("Invalid Action: User {} tried to cancel a rejected reservation {}", currentUserUuid, reservationUuid);
            throw new BusinessRuleException("This reservation has already been rejected and cannot be cancelled.");
        }

        if (reservation.getStatus().equals(ReservationStatus.CONFIRMED)) {
            BigDecimal refundFactor = isAdmin ? BigDecimal.ONE : BigDecimal.valueOf(0.9);
            BigDecimal refundAmount = reservation.getAmount().multiply(refundFactor);

            log.info("Processing Refund for confirmed reservation {}: Total Amount: {}, Refundable: {} (Factor: {})",
                    reservationUuid, reservation.getAmount(), refundAmount, refundFactor);
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        repository.save(reservation);

        log.info("Reservation {} successfully cancelled by {}", reservationUuid, isAdmin ? "ADMIN" : "OWNER");
        sendCancellationNotifications(reservation, isAdmin);
    }

    @Override
    public ReservationFindDTO get(@NonNull UUID reservationUuid, @NonNull UUID currentUserUuid, boolean isAdmin) {
        log.info("Fetching details for Reservation UUID: {}", reservationUuid);

        Reservation reservation = repository.findByUuid(reservationUuid)
                .orElseThrow(() -> {
                    log.error("Fetch failed: Reservation not found with UUID: {}", reservationUuid);
                    return new ResourceNotFoundException("Reservation", "identifier", reservationUuid.toString());
                });

        boolean isOwner = reservation.getTourist().getUuid().equals(currentUserUuid);
        boolean isAssignedGuide = reservation.getGuide() != null &&
                reservation.getGuide().getUuid().equals(currentUserUuid);

        if (!isOwner && !isAssignedGuide && !isAdmin) {
            log.error("Security Violation: Unauthorized fetch attempt for Reservation {} by User {}",
                    reservationUuid, currentUserUuid);
            throw new UnauthorizedActionException("Access denied: You are not authorized to view this reservation.");
        }

        log.debug("Reservation found: Status={}, Tourist={}",
                reservation.getStatus(), reservation.getTourist().getUuid());

        return mapper.toFindDto(reservation);
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

    private void sendReservationNotifications(Tour tour, Tourist tourist, Guide guide) {
        log.info("Sending notification to Tourist: {} and Guide: {}", tourist.getUuid(), guide.getUuid());

        String touristMessage = String.format("Your booking request for the tour '%s' has been received successfully.", tour.getTitle());
        notificationService.create("Booking Confirmation", touristMessage, tourist.getUuid());

        String guideMessage = String.format("A new tour '%s' has been assigned to you by %s.",
                tour.getTitle(), tourist.getFullName());
        notificationService.create("New Tour Assigned", guideMessage, guide.getUuid());
    }

    private void sendUpdateNotifications(Reservation res, Guide oldG, Guide newG, boolean isNewG, boolean isNewD) {
        Tourist tourist = res.getTourist();
        Tour tour = res.getTour();

        String touristMsg = String.format("Your reservation for '%s' has been updated successfully.", tour.getTitle());
        notificationService.create("Reservation Updated", touristMsg, tourist.getUuid());

        if (isNewG) {
            String oldGMsg = String.format("The tour '%s' (Date: %s) is no longer assigned to you.", tour.getTitle(), res.getStartDate());
            notificationService.create("Assignment Cancelled", oldGMsg, oldG.getUuid());

            String newGMsg = String.format("A new tour '%s' has been assigned to you for %s.", tour.getTitle(), res.getStartDate());
            notificationService.create("New Tour Assigned", newGMsg, newG.getUuid());
        } else if (isNewD) {
            String dateMsg = String.format("The schedule for tour '%s' has been changed to %s.", tour.getTitle(), res.getStartDate());
            notificationService.create("Schedule Changed", dateMsg, newG.getUuid());
        }
    }

    private void sendCancellationNotifications(Reservation res, boolean isAdminAction) {
        Tourist tourist = res.getTourist();
        Guide guide = res.getGuide();
        Tour tour = res.getTour();

        String touristSubject = "Reservation Cancellation";
        String touristMsg = isAdminAction
                ? String.format("Your reservation for tour '%s' has been cancelled by the administration.", tour.getTitle())
                : String.format("You have successfully cancelled your reservation for tour '%s'.", tour.getTitle());
        notificationService.create(touristSubject, touristMsg, tourist.getUuid());

        String guideSubject = "Tour Assignment Cancelled";
        String guideMsg = String.format("Important: The tour '%s' scheduled for %s has been cancelled.",
                tour.getTitle(), res.getStartDate());
        notificationService.create(guideSubject, guideMsg, guide.getUuid());

        log.debug("Cancellation notifications dispatched to Tourist: {} and Guide: {}", tourist.getUuid(), guide.getUuid());
    }
}
