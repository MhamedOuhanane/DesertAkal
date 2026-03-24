package com.desertakal.desertakal.controller;

import com.desertakal.desertakal.exception.custom.BadRequestException;
import com.desertakal.desertakal.model.dto.guide.GuideCreateDTO;
import com.desertakal.desertakal.model.dto.guide.GuideDTO;
import com.desertakal.desertakal.model.dto.guide.GuideFindDTO;
import com.desertakal.desertakal.model.dto.guide.GuideUpdateDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.dto.responce.StandardResponseDTO;
import com.desertakal.desertakal.model.enums.ReservationStatus;
import com.desertakal.desertakal.model.enums.ReviewableType;
import com.desertakal.desertakal.service.interfaces.GuideService;
import com.desertakal.desertakal.service.interfaces.ReservationService;
import com.desertakal.desertakal.service.interfaces.ReviewService;
import com.desertakal.desertakal.service.interfaces.TourService;
import io.swagger.v3.oas.annotations.Operation;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/guides")
@RequiredArgsConstructor
@Slf4j
public class GuideController {
    private final GuideService service;
    private final TourService tourService;
    private final ReservationService reservationService;
    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull GuideFindDTO>> create(
            @Valid @RequestBody GuideCreateDTO dto,
            HttpServletRequest request
    ) {
        log.info("REST request to create a new Guide with email: {}", dto.getEmail());

        var result = service.create(dto);

        log.info("Successfully created Guide with UUID: {}", result.getUuid());

        return ResponseEntity.status(HttpStatus.CREATED).body(buildResponse(
                "Guide account has been created successfully. A welcome email with credentials has been sent.",
                HttpStatus.CREATED,
                request, result
        ));
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull GuideFindDTO>> show(
            @NonNull @PathVariable UUID uuid,
            @NonNull HttpServletRequest request
    ) {
        log.info("REST request to get Guide by UUID: {} [Path: {}]", uuid, request.getServletPath());

        var result = service.find(uuid);

        log.info("Successfully retrieved guide details for UUID: {}", uuid);

        return ResponseEntity.ok(buildResponse(
                "Guide details retrieved successfully",
                HttpStatus.OK,
                request, result));
    }

    @GetMapping
    @PreAuthorize("permitAll()")
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

        log.info("Successfully processed Guides request for path: {}", request.getServletPath());

        return ResponseEntity.ok(buildResponse(
                "Guides retrieved successfully",
                HttpStatus.OK,
                request, result));
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

        log.info("Guide with UUID: {} has been successfully patched via {}", uuid, request.getServletPath());

        return ResponseEntity.ok(buildResponse(
                "Guide info updated successfully",
                HttpStatus.OK,
                request, result));
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

        log.info("Successfully returned {} tours for Guide UUID: {} [Status: 200]",
                result.getTotalElements(), uuid);

        return ResponseEntity.ok(buildResponse(
                message,
                HttpStatus.OK,
                request, result));
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

        log.info("Successfully returned {} assignments for Guide: {} | Page: {}",
                result.getTotalElements(), uuid, pageable.getPageNumber());

        return ResponseEntity.ok(buildResponse(
                "Your assigned reservations have been retrieved successfully",
                HttpStatus.OK,
                request, result));
    }

    @GetMapping("/available")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<@NonNull StandardResponseDTO<List<GuideDTO>>> getAvailableGuides(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false, defaultValue = "") String language,
            HttpServletRequest request
    ) {
        log.info("REST request to get available guides from {} to {}", startDate, endDate);

        if (endDate.isBefore(startDate)) {
            throw new BadRequestException("End date must be after start date");
        }

        var result = service.findAvailable(startDate, endDate, language);

        return ResponseEntity.ok(buildResponse(
                "Available guides retrieved successfully",
                HttpStatus.OK,
                request,
                result));
    }

    @GetMapping("/{uuid}/reviews")
    @PreAuthorize("permitAll()")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull PaginationDTO>> getReviews(
            @PathVariable UUID uuid,
            @RequestParam(required = false) BigDecimal minRating,
            Pageable pageable,
            HttpServletRequest request
    ) {
        log.info("Guide [{}] is accessing their own reviews. Filter: minRating={}", uuid, minRating);

        log.debug("Guide reviews pagination - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());

        PaginationDTO result = reviewService.getByReviewable(uuid, ReviewableType.GUIDE, minRating, pageable);

        log.info("Successfully fetched {} reviews for Guide [{}].", result.getTotalElements(), uuid);

        return ResponseEntity.ok(buildResponse(
                "Reviews retrieved.",
                HttpStatus.OK,
                request, result));
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
