package com.desertakal.desertakal.controller;

import com.desertakal.desertakal.Security.user.CustomUserDetails;
import com.desertakal.desertakal.exception.custom.FileUploadException;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.dto.responce.StandardResponseDTO;
import com.desertakal.desertakal.model.dto.tourist.TouristDTO;
import com.desertakal.desertakal.model.dto.tourist.TouristUpdateDTO;
import com.desertakal.desertakal.model.dto.user.UserFindDTO;
import com.desertakal.desertakal.model.dto.user.UserUpdateDTO;
import com.desertakal.desertakal.model.enums.PaymentStatus;
import com.desertakal.desertakal.model.enums.ReservationStatus;
import com.desertakal.desertakal.service.interfaces.*;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/tourists")
@RequiredArgsConstructor
@Slf4j
public class TouristController {
    private final TouristService service;
    private final TourService tourService;
    private final ReservationService reservationService;
    private final PaymentService paymentService;
    private final ReviewService reviewService;

    @GetMapping("/{uuid}")
    @PreAuthorize("@ownerSecurityService.isOwner(#uuid, authentication, true)")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull TouristDTO>> find(
            @NonNull @PathVariable UUID uuid,
            @NonNull HttpServletRequest request
    ) {
        log.info("REST request to get Tourist details for UUID: {} [Path: {}]",
                uuid, request.getServletPath());

        var result = service.find(uuid);

        log.debug("Found tourist data: {}", result);

        log.info("Successfully retrieved tourist for UUID: {}", uuid);
        return ResponseEntity.ok(buildResponse("Tourist details retrieved successfully", HttpStatus.OK, request, result));
    }

    @PatchMapping("/{uuid}/avatar")
    @PreAuthorize("@ownerSecurityService.isOwner(#uuid, authentication, false )")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull TouristDTO>> updateAvatar(
            @PathVariable UUID uuid,
            @RequestParam MultipartFile avatar,
            HttpServletRequest request
    ) {
        log.info("REST request to update avatar for Tourist: {} [File: {}, Size: {} bytes]",
                uuid, avatar.getOriginalFilename(), avatar.getSize());

        if (avatar.isEmpty()) {
            log.warn("Attempt to upload empty file for Tourist: {}", uuid);
            throw new FileUploadException("The uploaded file is empty. Please select a valid image.");
        }

        var result = service.updateAvatar(uuid, avatar);

        log.info("Successfully updated avatar for Tourist: {}. New Path stored.", uuid);
        return ResponseEntity.ok(buildResponse("Profile Avatar updated successfully", HttpStatus.OK, request, result));
    }

    @PatchMapping("/{uuid}")
    @PreAuthorize("@ownerSecurityService.isOwner(#uuid, authentication, true)")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull TouristDTO>> update(
            @NonNull @PathVariable UUID uuid,
            @NonNull @Valid @RequestBody TouristUpdateDTO dto,
            @NonNull HttpServletRequest request
    ) {
        log.info("REST request to patch Tourist : {} [Path: {}]", uuid, request.getServletPath());

        log.debug("Update payload for Tourist {}: {}", uuid, dto);

        var result = service.update(uuid, dto);

        log.info("Tourist with UUID: {} has been successfully patched via {}", uuid, request.getServletPath());

        return ResponseEntity.ok(buildResponse("Profile updated successfully", HttpStatus.OK, request, result));
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
        log.info("REST request to get all booked tours for Tourist UUID: {} [Page: {}, Size: {}, SortBy: {}]",
                uuid, page, size, sortBy);

        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        var result = tourService.findAllByTourist(uuid, pageable);

        String message = String.format("Successfully retrieved %d tour(s) for the requested tourist profile.",
                result.getTotalElements());

        log.info("Successfully returned {} tours for Tourist UUID: {} [Status: 200]",
                result.getTotalElements(), uuid);

        return ResponseEntity.ok(buildResponse(message, HttpStatus.OK, request, result));
    }

    @GetMapping("/{uuid}/reservations")
    @PreAuthorize("@ownerSecurityService.isOwner(#uuid, authentication, false)")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull PaginationDTO>> getReservations(
            @PathVariable UUID uuid,
            @RequestParam(required = false) String tour,
            @RequestParam(required = false) String guide,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @ParameterObject Pageable pageable,
            @NonNull HttpServletRequest request
    ) {
        log.info("Fetching personal reservations for user: {} | Path: {}", uuid, request.getServletPath());

        PaginationDTO result = reservationService.getByTourist(uuid, tour, guide, status, startDate, endDate, pageable);

        log.info("Successfully dispatched {} reservations to owner: {}", result.getTotalElements(), uuid);

        return ResponseEntity.ok(buildResponse("Your reservations have been retrieved successfully", HttpStatus.OK, request, result));
    }

    @GetMapping("/{uuid}/payments")
    @PreAuthorize("@ownerSecurityService.isOwner(#uuid, authentication, false ) and hasRole('TOURIST')")
    public ResponseEntity<@NonNull StandardResponseDTO<PaginationDTO>> myPayments(
            @PathVariable UUID uuid,
            @RequestParam(required = false) PaymentStatus status,
            @ParameterObject Pageable pageable,
            @NonNull HttpServletRequest request) {

        log.info("REST request by tourist {} to get payments with status: {}", uuid, status);

        PaginationDTO result = paymentService.getPaymentsByTourist(uuid, status, pageable);

        return ResponseEntity.ok(buildResponse("Personal payments history retrieved", HttpStatus.OK, request, result));
    }


    @GetMapping("/{uuid}/reviews")
    @PreAuthorize("permitAll()")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull PaginationDTO>> getReviews(
            @PathVariable UUID uuid,
            @RequestParam(required = false) BigDecimal minRating,
            Pageable pageable,
            HttpServletRequest request) {

        log.info("REST request to fetch review history for Tourist [{}]. Filter: minRating={}",
                uuid, minRating != null ? minRating : "NONE");

        log.debug("Tourist reviews pagination - Page: {}, Size: {}, Sort: {}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        PaginationDTO result = reviewService.getByTourist(uuid, minRating, pageable);

        log.info("Successfully retrieved {} reviews written by Tourist [{}].",
                result.getTotalElements(), uuid);

        return ResponseEntity.ok(buildResponse(
                "Tourist review history retrieved successfully.",
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
