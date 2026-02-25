package com.desertakal.desertakal.controller;

import com.desertakal.desertakal.model.dto.guide.GuideCreateDTO;
import com.desertakal.desertakal.model.dto.guide.GuideFindDTO;
import com.desertakal.desertakal.model.dto.guide.GuideUpdateDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.dto.responce.StandardResponseDTO;
import com.desertakal.desertakal.model.enums.ReservationStatus;
import com.desertakal.desertakal.service.interfaces.GuideService;
import com.desertakal.desertakal.service.interfaces.ReservationService;
import com.desertakal.desertakal.service.interfaces.TourService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/guides")
@RequiredArgsConstructor
@Slf4j
public class GuideController {
    private final GuideService service;
    private final TourService tourService;
    private final ReservationService reservationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull GuideFindDTO>> create(
            @Valid @RequestBody GuideCreateDTO dto,
            HttpServletRequest request
    ) {
        log.info("REST request to create a new Guide with email: {}", dto.getEmail());

        var result = service.create(dto);

        var response = StandardResponseDTO.<GuideFindDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("Guide account has been created successfully. A welcome email with credentials has been sent.")
                .status(201)
                .data(result)
                .path(request.getServletPath())
                .build();

        log.info("Successfully created Guide with UUID: {}", result.getUuid());

        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull GuideFindDTO>> show(
            @NonNull @PathVariable UUID uuid,
            @NonNull HttpServletRequest request
    ) {
        log.info("REST request to get Guide by UUID: {} [Path: {}]", uuid, request.getServletPath());

        var result = service.find(uuid);

        var response = StandardResponseDTO.<GuideFindDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("Guide details retrieved successfully")
                .status(200)
                .path(request.getServletPath())
                .data(result)
                .build();

        log.info("Successfully retrieved guide details for UUID: {}", uuid);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull PaginationDTO>> shows(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String language,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastLoginAt") String sortBy,
            @RequestParam(defaultValue = "asc") String order,
            @NonNull HttpServletRequest request
    ) {
        log.info("REST request to get a page of Guides [Page: {}, Size: {}] from path: {}",
                page, size, request.getServletPath());

        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        var result = service.findAll(search, language, pageable);

        var response = StandardResponseDTO.<PaginationDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("Guides retrieved successfully")
                .status(200)
                .path(request.getServletPath())
                .data(result)
                .build();

        log.info("Successfully processed Guides request for path: {}", request.getServletPath());

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull GuideFindDTO>> update(
            @NonNull @PathVariable UUID uuid,
            @NonNull @Valid @RequestBody GuideUpdateDTO dto,
            @NonNull HttpServletRequest request
    ) {
        log.info("REST request to patch Guide : {} [Path: {}]", uuid, request.getServletPath());

        log.debug("Update payload for Guide {}: {}", uuid, dto);

        var result = service.update(uuid, dto);

        var response = StandardResponseDTO.<GuideFindDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("Guide info updated successfully")
                .status(200)
                .path(request.getServletPath())
                .data(result)
                .build();

        log.info("Guide with UUID: {} has been successfully patched via {}", uuid, request.getServletPath());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{uuid}/tours")
    @PreAuthorize("@ownerSecurityService.isOwner(#uuid, authentication, true)")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull PaginationDTO>> getTours(
            @NonNull @PathVariable UUID uuid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            @NonNull HttpServletRequest request
    ) {
        log.info("REST request to get all tours assigned for Guide UUID: {} [Page: {}, Size: {}, SortBy: {}]",
                uuid, page, size, sortBy);

        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        var result = tourService.findAllByGuide(uuid, pageable);

        String message = String.format("Successfully retrieved %d tour(s) for the requested guide profile.",
                result.getTotalElements());

        var response = StandardResponseDTO.<PaginationDTO>builder()
                .timestamp(LocalDateTime.now())
                .message(message)
                .status(200)
                .path(request.getServletPath())
                .data(result)
                .build();

        log.info("Successfully returned {} tours for Guide UUID: {} [Status: 200]",
                result.getTotalElements(), uuid);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{uuid}/reservations")
    @PreAuthorize("@ownerSecurityService.isOwner(#uuid, authentication, false)")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull PaginationDTO>> getReservations(
            @PathVariable UUID uuid,
            @RequestParam(required = false) String tour,
            @RequestParam(required = false) String tourist,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @ParameterObject Pageable pageable,
            @NonNull HttpServletRequest request
    ) {
        log.info("REST request for Guide to fetch assigned reservations | Guide UUID: {} | Path: {}",
                uuid, request.getServletPath());

        log.debug("Guide filters -> Tour: {}, Tourist: {}, Status: {}, DateRange: [{} to {}]",
                tour, tourist, status, startDate, endDate);

        PaginationDTO result = reservationService.getByGuide(uuid, tour, tourist, status, startDate, endDate, pageable);

        var response = StandardResponseDTO.<PaginationDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("Your assigned reservations have been retrieved successfully")
                .status(HttpStatus.OK.value())
                .path(request.getServletPath())
                .data(result)
                .build();

        log.info("Successfully returned {} assignments for Guide: {} | Page: {}",
                result.getTotalElements(), uuid, pageable.getPageNumber());

        return ResponseEntity.ok(response);
    }
}
