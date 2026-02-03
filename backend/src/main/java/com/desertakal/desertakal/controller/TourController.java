package com.desertakal.desertakal.controller;

import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.dto.responce.StandardResponseDTO;
import com.desertakal.desertakal.model.dto.tour.TourCreateDTO;
import com.desertakal.desertakal.model.dto.tour.TourFindDTO;
import com.desertakal.desertakal.model.dto.tour.TourUpdateDTO;
import com.desertakal.desertakal.service.interfaces.CityService;
import com.desertakal.desertakal.service.interfaces.TourService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
@Slf4j
public class TourController {
    private final TourService service;
    private final CityService cityService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull TourFindDTO>> create(
            @RequestPart("tour") @Valid TourCreateDTO dto,
            @RequestPart("image") MultipartFile image,
            HttpServletRequest request
    ) {
        log.info("REST request to create a new Tour with title: {}", dto.getTitle());

        var result = service.create(dto, image);

        var response = StandardResponseDTO.<TourFindDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("Tour account has been created successfully.")
                .status(201)
                .data(result)
                .path(request.getServletPath())
                .build();

        log.info("Successfully created Tour with UUID: {}", result.getUuid());

        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull TourFindDTO>> show(
            @PathVariable UUID uuid,
            HttpServletRequest request
    ) {
        log.info("REST request to get Tour by UUID: {} [Path: {}]", uuid, request.getServletPath());

        var result = service.find(uuid);

        var response = StandardResponseDTO.<TourFindDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("Tour details retrieved successfully")
                .status(200)
                .path(request.getServletPath())
                .data(result)
                .build();

        log.info("Successfully retrieved tour details for UUID: {}", uuid);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull PaginationDTO>> shows(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String durationStr,
            @RequestParam(defaultValue = "0") @Positive BigDecimal minRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String order,
            HttpServletRequest request
    ) {
        log.info("REST request to get all Tours [Search: '{}', Page: {}, Size: {}, SortBy: {}, Order: {}]",
                search != null ? search : "ALL", page, size, sortBy, order);

        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        var result = service.findAll(search, city, durationStr, minRating, pageable);

        var response = StandardResponseDTO.<PaginationDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("Tours list retrieved successfully")
                .status(200)
                .data(result)
                .path(request.getServletPath())
                .build();

        log.info("Successfully fetched {} cities out of {} total elements [Status: 200]",
                result.getTotalElements(), result.getTotalElements());

        return ResponseEntity.ok(response);
    }

}
