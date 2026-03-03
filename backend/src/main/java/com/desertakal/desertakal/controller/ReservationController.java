package com.desertakal.desertakal.controller;

import com.desertakal.desertakal.Security.user.CustomUserDetails;
import com.desertakal.desertakal.model.dto.reservation.ReservationCreateDTO;
import com.desertakal.desertakal.model.dto.reservation.ReservationFindDTO;
import com.desertakal.desertakal.model.dto.reservation.ReservationUpdateDTO;
import com.desertakal.desertakal.model.dto.reservation.ReservationVerificationDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.dto.responce.StandardResponseDTO;
import com.desertakal.desertakal.model.enums.ReservationStatus;
import com.desertakal.desertakal.service.interfaces.PaymentService;
import com.desertakal.desertakal.service.interfaces.ReservationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Slf4j
public class ReservationController {
    private final ReservationService service;
    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasRole('TOURIST')")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull ReservationFindDTO>> create(
            @Valid @RequestBody ReservationCreateDTO dto,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest request
    ) {
        log.info("REST request to create Reservation | User: {} | Tour: {} | Path: {}",
                currentUser.getUuid(), dto.getTourUuid(), request.getServletPath());

        log.debug("Processing reservation with details: Number of people: {}, Start Date: {}",
                dto.getNumberPeople(), dto.getStartDate());

        ReservationFindDTO result = service.create(dto, currentUser.getUuid());

        log.info("Reservation created successfully for Tourist: {} | Reservation UUID: {}",
                currentUser.getUuid(), result.getUuid());

        return ResponseEntity.status(HttpStatus.CREATED).body(buildResponse("Reservation created successfully", HttpStatus.CREATED, request, result));

    }

    @PatchMapping("/{uuid}")
    @PreAuthorize("hasRole('TOURIST')")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull ReservationFindDTO>> update(
            @PathVariable UUID uuid,
            @Valid @RequestBody ReservationUpdateDTO dto,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest request
    ) {
        log.info("REST request to update Reservation: {} | User: {} | Path: {}",
                uuid, currentUser.getUuid(), request.getServletPath());

        ReservationFindDTO result = service.update(uuid, dto, currentUser.getUuid());

        log.info("Reservation updated successfully for Tourist: {} | Reservation UUID: {}",
                result.getTourUuid(), result.getUuid());

        return ResponseEntity.ok(buildResponse("Reservation updated successfully", HttpStatus.OK, request, result));
    }

    @PatchMapping("/{uuid}/cancel")
    @PreAuthorize("hasAnyRole('TOURIST', 'ADMIN')")
    public ResponseEntity<@NonNull StandardResponseDTO<Void>> cancel(
            @PathVariable UUID uuid,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest request
    ) {
        log.info("REST request to cancel Reservation: {} | Requested by: {} | Path: {}",
                uuid, currentUser.getUuid(), request.getServletPath());

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));

        service.cancel(uuid, currentUser.getUuid(), isAdmin);

        log.info("Reservation {} cancelled successfully", uuid);
        return ResponseEntity.ok(buildResponse("Reservation has been cancelled successfully", HttpStatus.OK, request, null));
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<@NonNull StandardResponseDTO<ReservationFindDTO>> show(
            @PathVariable UUID uuid,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest request
    ) {
        log.info("REST request to fetch Reservation details: {} | Requested by: {} | Path: {}",
                uuid, currentUser.getUuid(), request.getServletPath());

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));

        ReservationFindDTO result = service.get(uuid, currentUser.getUuid(), isAdmin);

        log.info("Successfully dispatched details for Reservation: {} to User: {}", uuid, currentUser.getUuid());
        return ResponseEntity.ok(buildResponse("Reservation details retrieved successfully", HttpStatus.OK, request, result));
    }

    @GetMapping("/ref/{reference}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<@NonNull StandardResponseDTO<ReservationFindDTO>> show(
            @PathVariable String reference,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest request
    ) {
        log.info("REST request to fetch Reservation by Reference: {} | Requested by User: {} | Path: {}",
                reference, currentUser.getUuid(), request.getServletPath());

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));

        ReservationFindDTO result = service.get(reference, currentUser.getUuid(), isAdmin);

        log.info("Successfully dispatched details for Reference: {} to User: {}",
                reference, currentUser.getUuid());
        return ResponseEntity.ok(buildResponse("Reservation details retrieved successfully via Reference", HttpStatus.OK, request, result));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<@NonNull StandardResponseDTO<PaginationDTO>> shows(
            @RequestParam(required = false) String tour,
            @RequestParam(required = false) String guide,
            @RequestParam(required = false) String tourist,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @ParameterObject Pageable pageable,
            HttpServletRequest request
    ) {
        log.info("REST request to fetch all reservations | Filters -> Tour: {}, Guide: {}, Tourist: {}, Status: {} | Path: {}",
                tour, guide, tourist, status, request.getServletPath());

        PaginationDTO result = service.getAll(tour, guide, tourist, status, startDate, endDate, pageable);

        log.info("Successfully returned {} reservations for page {}",
                result.getTotalElements(), pageable.getPageNumber());
        return ResponseEntity.ok(buildResponse("Reservations retrieved successfully", HttpStatus.OK, request, result));
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<@NonNull StandardResponseDTO<Void>> delete(
            @PathVariable UUID uuid,
            HttpServletRequest request
    ) {
        log.info("REST request to DELETE Reservation with UUID: {} [Requested by Path: {}]",
                uuid, request.getServletPath());

        service.delete(uuid);

        log.info("Successfully deleted Reservation with UUID: {} [Status: 200 OK]", uuid);
        return ResponseEntity.ok(buildResponse("Payments", HttpStatus.OK, request, null));
    }

    @GetMapping("/{uuid}/download")
    public ResponseEntity<byte @NonNull []> downloadPdf(
            @PathVariable UUID uuid,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        log.info("REST request to download PDF for reservation: {}", uuid);

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));

        byte[] pdfContent = service.getReservationPdfContent(uuid, currentUser.getUuid(), isAdmin);

        String fileName = "Voucher_DesertAkal_" + uuid.toString().substring(0, 8) + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfContent);
    }

    @GetMapping("/verify/{uuid}")
    public ResponseEntity<@NonNull StandardResponseDTO<ReservationVerificationDTO>> verify(
            @PathVariable UUID uuid,
            HttpServletRequest request
    ) {
        log.info("Public verification request for reservation: {}", uuid);

        ReservationVerificationDTO result = service.verifyReservation(uuid);

        String message = result.isValid()
                ? "Reservation is valid and confirmed."
                : "Warning: This reservation is " + result.getStatus().toLowerCase();

        log.info("Verification result for {}: {}", uuid, result.getStatus());
        return ResponseEntity.ok(buildResponse(message, HttpStatus.OK, request, result));
    }

    @GetMapping("/{uuid}/payments")
    @PreAuthorize("hasAnyRole('TOURIST','ADMIN')")
    public ResponseEntity<@NonNull StandardResponseDTO<PaginationDTO>> byReservation(
            @PathVariable UUID uuid,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @ParameterObject Pageable pageable,

            @NonNull HttpServletRequest request) {

        log.info("REST request to get payments for reservation: {}", uuid);
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));

        PaginationDTO result = paymentService.getPaymentsByReservation(uuid, pageable, currentUser.getUuid(), isAdmin);

        return ResponseEntity.ok(buildResponse("Payments retrieved for reservation", HttpStatus.OK, request, result));
    }

    private <T> StandardResponseDTO<T> buildResponse(String message, HttpStatus status, HttpServletRequest request, T data) {
        return StandardResponseDTO.<T>builder()
                .timestamp(LocalDateTime.now())
                .message(message)
                .status(status.value())
                .path(request.getServletPath())
                .data(data)
                .build();
    }
}
