package com.desertakal.desertakal.controller;

import com.desertakal.desertakal.Security.user.CustomUserDetails;
import com.desertakal.desertakal.model.dto.reservation.ReservationCreateDTO;
import com.desertakal.desertakal.model.dto.reservation.ReservationFindDTO;
import com.desertakal.desertakal.model.dto.responce.StandardResponseDTO;
import com.desertakal.desertakal.service.interfaces.ReservationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

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
}
