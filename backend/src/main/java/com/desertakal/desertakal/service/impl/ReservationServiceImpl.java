package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.exception.custom.*;
import com.desertakal.desertakal.model.dto.reservation.ReservationCreateDTO;
import com.desertakal.desertakal.model.dto.reservation.ReservationFindDTO;
import com.desertakal.desertakal.model.dto.reservation.ReservationUpdateDTO;
import com.desertakal.desertakal.model.dto.reservation.ReservationVerificationDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.entity.*;
import com.desertakal.desertakal.model.enums.ReservationStatus;
import com.desertakal.desertakal.model.mapper.ReservationMapper;
import com.desertakal.desertakal.repository.*;
import com.desertakal.desertakal.service.interfaces.DocumentGeneratorService;
import com.desertakal.desertakal.service.interfaces.FileStorageService;
import com.desertakal.desertakal.service.interfaces.NotificationService;
import com.desertakal.desertakal.service.interfaces.ReservationService;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final DocumentGeneratorService documentGeneratorService;
    private final FileStorageService fileStorageService;

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

        try {
            Reservation newReservation = repository.save(reservation);

            log.info("Reservation saved. Generating PDF and QR assets...");
            documentGeneratorService.generateConfirmationAssets(newReservation);

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

        documentGeneratorService.generateConfirmationAssets(reservation);

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
    public ReservationFindDTO get(@NonNull String reference, @NonNull UUID currentUserUuid, boolean isAdmin) {
        log.info("Fetching details for Reservation reference: {}", reference);

        Reservation reservation = repository.findByReference(reference)
                .orElseThrow(() -> {
                    log.error("Fetch failed: Reservation not found with reference: {}", reference);
                    return new ResourceNotFoundException("Reservation", "reference", reference);
                });

        boolean isOwner = reservation.getTourist().getUuid().equals(currentUserUuid);
        boolean isAssignedGuide = reservation.getGuide() != null &&
                reservation.getGuide().getUuid().equals(currentUserUuid);

        if (!isOwner && !isAssignedGuide && !isAdmin) {
            log.error("Security Violation: Unauthorized fetch attempt for Reservation reference {} by User {}",
                    reference, currentUserUuid);
            throw new UnauthorizedActionException("Access denied: You are not authorized to view this reservation.");
        }

        log.debug("Reservation found by reference {}: Status={}, Tourist={}",
                reference, reservation.getStatus(), reservation.getTourist().getUuid());

        return mapper.toFindDto(reservation);
    }

    @Override
    public PaginationDTO getAll(String tour, String guide, String tourist, ReservationStatus status, LocalDateTime startDate, LocalDateTime endDate, @NonNull Pageable pageable) {
        log.info("REST Request to fetch all Reservations with filters | Tour: {}, Status: {}, Period: [{} to {}] | Page: {}, Size: {}",
                tour, status, startDate, endDate, pageable.getPageNumber(), pageable.getPageSize());

        Specification<@NonNull Reservation> spec = getSpecification(null, null, tour, guide, tourist, status, startDate, endDate);

        log.debug("Executing paginated database query for Reservations...");
        Page<@NonNull Reservation> reservationPages = repository.findAll(spec, pageable);

        log.info("Fetch completed: Found total of {} reservations | Returning page {} of {}",
                reservationPages.getTotalElements(), reservationPages.getNumber(), reservationPages.getTotalPages());

        return PaginationDTO.builder()
                .content(mapper.toDtos(reservationPages.getContent()))
                .page(reservationPages.getNumber())
                .size(reservationPages.getSize())
                .totalElements(reservationPages.getTotalElements())
                .totalPages(reservationPages.getTotalPages())
                .isFirst(reservationPages.isFirst())
                .isLast(reservationPages.isLast())
                .build();
    }

    @Override
    public PaginationDTO getByTourist(@NonNull UUID touristUuid, String tour, String guide, ReservationStatus status,
                                      LocalDateTime startDate, LocalDateTime endDate, @NonNull Pageable pageable) {
        log.info("Fetching reservations for Tourist: {} | Filters -> Tour: {}, Guide: {}, Status: {}",
                touristUuid, tour, guide, status);

        Specification<@NonNull Reservation> spec = getSpecification(touristUuid, null, tour, guide, null, status, startDate, endDate);

        log.debug("Executing paginated query for Tourist reservations. Page: {}, Size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<@NonNull Reservation> reservationPages = repository.findAll(spec, pageable);

        log.info("Successfully retrieved {} reservations for Tourist: {}",
                reservationPages.getTotalElements(), touristUuid);

        return PaginationDTO.builder()
                .content(mapper.toDtos(reservationPages.getContent()))
                .page(reservationPages.getNumber())
                .size(reservationPages.getSize())
                .totalElements(reservationPages.getTotalElements())
                .totalPages(reservationPages.getTotalPages())
                .isFirst(reservationPages.isFirst())
                .isLast(reservationPages.isLast())
                .build();
    }

    @Override
    public PaginationDTO getByGuide(@NonNull UUID guideUuid, String tour, String tourist, ReservationStatus status,
                                    LocalDateTime startDate, LocalDateTime endDate, @NonNull Pageable pageable) {
        log.info("Fetching assigned tours for Guide: {} | Filters -> Tour: {}, Tourist: {}, Status: {}",
                guideUuid, tour, tourist, status);

        Specification<@NonNull Reservation> spec = getSpecification(null, guideUuid, tour, null, tourist, status, startDate, endDate);

        log.debug("Executing paginated query for Guide assignments. Page: {}, Size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<@NonNull Reservation> reservationPages = repository.findAll(spec, pageable);

        log.info("Successfully retrieved {} assignments for Guide: {}",
                reservationPages.getTotalElements(), guideUuid);

        return PaginationDTO.builder()
                .content(mapper.toDtos(reservationPages.getContent()))
                .page(reservationPages.getNumber())
                .size(reservationPages.getSize())
                .totalElements(reservationPages.getTotalElements())
                .totalPages(reservationPages.getTotalPages())
                .isFirst(reservationPages.isFirst())
                .isLast(reservationPages.isLast())
                .build();
    }

    @Override
    @Transactional
    public void delete(@NonNull UUID reservationUuid) {
        log.info("Initiating deletion attempt for Reservation: {}", reservationUuid);

        Reservation reservation = repository.findByUuid(reservationUuid)
                .orElseThrow(() -> {
                    log.error("Delete failed: Reservation {} not found", reservationUuid);
                    return new ResourceNotFoundException("Reservation", "identifier", reservationUuid.toString());
                });

        ReservationStatus status = reservation.getStatus();

        if (status.equals(ReservationStatus.CONFIRMED) || status.equals(ReservationStatus.COMPLETED)) {
            log.warn("Forbidden Action: Attempted to delete a {} reservation: {}", status, reservationUuid);
            throw new BusinessRuleException(String.format("Cannot delete a reservation that is already %s.", status.name().toLowerCase()));
        }

        try {
            if (reservation.getPdfUrl() != null) {
                fileStorageService.deleteFile(reservation.getPdfUrl());
                log.debug("Deleted associated PDF from storage: {}", reservation.getPdfUrl());
            }
            if (reservation.getQrCode() != null) {
                fileStorageService.deleteFile(reservation.getQrCode());
                log.debug("Deleted associated QR code from storage: {}", reservation.getQrCode());
            }
        } catch (Exception e) {
            log.error("Non-critical error: Failed to clean up files in MinIO for reservation {}: {}",
                    reservationUuid, e.getMessage());
        }

        repository.delete(reservation);

        log.info("Reservation {} successfully deleted (Original Status: {})", reservationUuid, status);
    }

    @Override
    public byte[] getReservationPdfContent(@NonNull UUID reservationUuid, @NonNull UUID currentUserUuid, boolean isAdmin) {
        log.info("Initiating PDF download request for Reservation: {} by User: {} (Admin: {})",
                reservationUuid, currentUserUuid, isAdmin);

        Reservation reservation = repository.findByUuid(reservationUuid)
                .orElseThrow(() -> {
                    log.error("Download failed: Reservation {} not found in database", reservationUuid);
                    return new ResourceNotFoundException("Reservation", "uuid", reservationUuid.toString());
                });

        boolean isOwner = reservation.getTourist().getUuid().equals(currentUserUuid);

        if (!isOwner && !isAdmin) {
            log.warn("Security Alert: Unauthorized download attempt on Reservation {} by User {}",
                    reservationUuid, currentUserUuid);
            throw new UnauthorizedActionException("You are not authorized to download this document.");
        }

        if (reservation.getPdfUrl() == null || reservation.getPdfUrl().isBlank()) {
            log.error("Integrity Error: Reservation {} exists but has no PDF path associated", reservationUuid);
            throw new ResourceNotFoundException("PDF Document", "reservation", reservationUuid.toString());
        }

        log.debug("Fetching file bytes from storage for path: {}", reservation.getPdfUrl());
        byte[] content = fileStorageService.downloadFile(reservation.getPdfUrl());

        if (content == null || content.length == 0) {
            log.error("Storage Error: MinIO returned empty or null content for path: {}", reservation.getPdfUrl());
            throw new DocumentGenerationException("The PDF file could not be retrieved from storage.");
        }

        log.info("PDF successfully retrieved for Reservation: {}. Size: {} bytes",
                reservationUuid, content.length);

        return content;
    }

    @Override
    public ReservationVerificationDTO verifyReservation(@NonNull UUID reservationUuid) {
        log.info("Verifying reservation authenticity: {}", reservationUuid);

        Reservation reservation = repository.findByUuid(reservationUuid)
                .orElseThrow(() -> {
                    log.error("Verification failed: Reservation {} not found in database", reservationUuid);
                    return new ResourceNotFoundException("Reservation", "uuid", reservationUuid.toString());
                });

        return mapper.toVerificationDto(reservation);
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

    private Specification<@NonNull Reservation> getSpecification(
            UUID touristUuid, UUID guideUuid,
            String tour, String guide, String tourist,
            ReservationStatus status, LocalDateTime startDate, LocalDateTime endDate) {
        return  (root, query, cb) -> {
            query.distinct(true);

            log.debug("Building dynamic Specification for Reservation search with parameters: [tour: {}, guide: {}, tourist: {}, status: {}, startDate: {}, startDate: {}]",
                    tour, guide, tourist, status, startDate, endDate);

            List<Predicate> predicates = new ArrayList<>();

            Join<Reservation, Tour> tourJoin = root.join("tour", JoinType.LEFT);
            Join<Reservation, Guide> guideJoin = root.join("guide", JoinType.LEFT);
            Join<Reservation, Tourist> touristJoin = root.join("tourist", JoinType.LEFT);

            if (touristUuid != null) {
                predicates.add(cb.equal(touristJoin.get("uuid"), touristUuid));
                log.debug("Filter applied: tourist.uuid = '{}'", touristUuid);
            }

            if (guideUuid != null) {
                predicates.add(cb.equal(guideJoin.get("uuid"), guideUuid));
                log.debug("Filter applied: guide.uuid = '{}'", guideUuid);
            }

            if (tour != null && !tour.isBlank()) {
                predicates.add(cb.like(cb.lower(tourJoin.get("title")), "%" + tour.toLowerCase() + "%"));
                log.debug("Filter added: title LIKE '%{}%'", tour);
            }

            if (guide != null && !guide.isBlank()) {
                String pattern = "%" + guide.toLowerCase() + "%";
                Expression<String> guideFullName = cb.concat(cb.concat(cb.lower(guideJoin.get("firstName")), " "), cb.lower(guideJoin.get("lastName")));
                predicates.add(cb.like(guideFullName, pattern));
                log.debug("Filter applied: guide full name matching '{}'", guide);
            }

            if (tourist != null && !tourist.isBlank()) {
                String pattern = "%" + tourist.toLowerCase() + "%";
                Expression<String> guideFullName = cb.concat(cb.concat(cb.lower(touristJoin.get("firstName")), " "), cb.lower(touristJoin.get("lastName")));
                predicates.add(cb.like(guideFullName, pattern));
                log.debug("Filter applied: tourist full name matching '{}'", tourist);
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
                log.debug("Filter added: status equal '%{}%'", status);
            }

            if (startDate != null) {
                LocalDateTime startOfDay = startDate.toLocalDate().atStartOfDay();
                LocalDateTime endOfDay = startDate.toLocalDate().atTime(23, 59, 59);

                predicates.add(cb.between(root.get("startDate"), startOfDay, endOfDay));

                log.debug("Filter applied: startDate BETWEEN '{}' AND '{}'", startOfDay, endOfDay);
            }

            if (endDate != null) {
                LocalDateTime startOfDay = endDate.toLocalDate().atStartOfDay();
                LocalDateTime endOfDay = endDate.toLocalDate().atTime(23, 59, 59);

                predicates.add(cb.between(root.get("endDate"), startOfDay, endOfDay));

                log.debug("Filter applied: endDate BETWEEN '{}' AND '{}'", startOfDay, endOfDay);
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
