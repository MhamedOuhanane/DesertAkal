package com.desertakal.desertakal.controller;

import com.desertakal.desertakal.model.dto.city.CityCreateDTO;
import com.desertakal.desertakal.model.dto.city.CityFindDTO;
import com.desertakal.desertakal.model.dto.city.CityUpdateDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.dto.responce.StandardResponseDTO;
import com.desertakal.desertakal.service.interfaces.CityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
@Slf4j
public class CityController {
    private final CityService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull CityFindDTO>> create(
            @Valid @RequestBody CityCreateDTO dto,
            HttpServletRequest request
    ) {
        log.info("REST request to create a new City with name: {}", dto.getName());

        var result = service.create(dto);

        var response = StandardResponseDTO.<CityFindDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("City account has been created successfully.")
                .status(201)
                .data(result)
                .path(request.getServletPath())
                .build();

        log.info("Successfully created City with UUID: {}", result.getUuid());

        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull CityFindDTO>> show(
            @NonNull @PathVariable UUID uuid,
            @NonNull HttpServletRequest request
    ) {
        log.info("REST request to get City by UUID: {} [Path: {}]", uuid, request.getServletPath());

        var result = service.find(uuid);

        var response = StandardResponseDTO.<CityFindDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("City details retrieved successfully")
                .status(200)
                .path(request.getServletPath())
                .data(result)
                .build();

        log.info("Successfully retrieved city details for UUID: {}", uuid);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull PaginationDTO>> shows(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String order,
            @NonNull HttpServletRequest request
    ) {
        log.info("REST request to get all Cities [Search: '{}', Page: {}, Size: {}, SortBy: {}, Order: {}]",
                search != null ? search : "ALL", page, size, sortBy, order);

        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        var result = service.findAll(search, pageable);

        var response = StandardResponseDTO.<PaginationDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("Cities list retrieved successfully")
                .status(200)
                .data(result)
                .path(request.getServletPath())
                .build();

        log.info("Successfully fetched {} cities out of {} total elements [Status: 200]",
                result.getTotalElements(), result.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull CityFindDTO>> update(
            @NonNull @PathVariable UUID uuid,
            @NonNull @Valid @RequestBody CityUpdateDTO dto,
            @NonNull HttpServletRequest request
    ) {
        log.info("REST request to PATCH City: {} [Data provided: {}]",
                uuid, dto.getName() != null ? "Name: " + dto.getName() : "Partial update (no name change)");

        var result = service.update(uuid, dto);

        var response = StandardResponseDTO.<CityFindDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("City updated successfully: " + result.getName())
                .status(200)
                .data(result)
                .path(request.getServletPath())
                .build();

        log.info("Successfully updated City with UUID: {} [Status: 200 OK]", uuid);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<@NonNull StandardResponseDTO<Void>> delete(
            @NonNull @PathVariable UUID uuid,
            @NonNull HttpServletRequest request
    ) {
        log.info("REST request to DELETE City with UUID: {} [Requested by Path: {}]",
                uuid, request.getServletPath());

        service.delete(uuid);

        var response = StandardResponseDTO.<Void>builder()
                .timestamp(LocalDateTime.now())
                .status(200)
                .message("City has been successfully deleted")
                .path(request.getServletPath())
                .build();

        log.info("Successfully deleted City with UUID: {} [Status: 200 OK]", uuid);

        return ResponseEntity.ok(response);
    }
}
