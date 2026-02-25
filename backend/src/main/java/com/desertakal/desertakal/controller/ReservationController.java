package com.desertakal.desertakal.controller;

import com.desertakal.desertakal.Security.user.CustomUserDetails;
import com.desertakal.desertakal.model.dto.reservation.ReservationCreateDTO;
import com.desertakal.desertakal.model.dto.reservation.ReservationFindDTO;
import com.desertakal.desertakal.model.dto.reservation.ReservationUpdateDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.dto.responce.StandardResponseDTO;
import com.desertakal.desertakal.model.enums.ReservationStatus;
import com.desertakal.desertakal.service.interfaces.ReservationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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

        var response = StandardResponseDTO.<ReservationFindDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("Reservation created successfully")
                .status(HttpStatus.CREATED.value())
                .data(result)
                .path(request.getServletPath())
                .build();

        log.info("Reservation created successfully for Tourist: {} | Reservation UUID: {}",
                currentUser.getUuid(), result.getUuid());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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

        var response = StandardResponseDTO.<ReservationFindDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("Reservation updated successfully")
                .status(HttpStatus.CREATED.value())
                .data(result)
                .path(request.getServletPath())
                .build();

        log.info("Reservation updated successfully for Tourist: {} | Reservation UUID: {}",
                result.getTourUuid(), result.getUuid());

        return ResponseEntity.status(200).body(response);
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

        var response = StandardResponseDTO.<Void>builder()
                .timestamp(LocalDateTime.now())
                .message("Reservation has been cancelled successfully")
                .status(HttpStatus.OK.value())
                .path(request.getServletPath())
                .build();

        log.info("Reservation {} cancelled successfully", uuid);

        return ResponseEntity.ok(response);
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

        var response = StandardResponseDTO.<ReservationFindDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("Reservation details retrieved successfully")
                .status(HttpStatus.OK.value())
                .data(result)
                .path(request.getServletPath())
                .build();

        log.info("Successfully dispatched details for Reservation: {} to User: {}", uuid, currentUser.getUuid());

        return ResponseEntity.ok(response);
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

        var response = StandardResponseDTO.<PaginationDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("Reservations retrieved successfully")
                .status(HttpStatus.OK.value())
                .data(result)
                .path(request.getServletPath())
                .build();

        log.info("Successfully returned {} reservations for page {}",
                result.getTotalElements(), pageable.getPageNumber());

        return ResponseEntity.ok(response);
    }
}
