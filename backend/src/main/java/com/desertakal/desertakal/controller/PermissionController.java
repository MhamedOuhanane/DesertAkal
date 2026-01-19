package com.desertakal.desertakal.controller;

import com.desertakal.desertakal.model.dto.permission.PermissionDTO;
import com.desertakal.desertakal.model.dto.permission.PermissionRequestDTO;
import com.desertakal.desertakal.model.dto.permission.PermissionUpdateDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.dto.responce.StandardResponseDTO;
import com.desertakal.desertakal.service.interfaces.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@Slf4j
public class PermissionController {
    private final PermissionService service;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull PaginationDTO>> shows(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String order,
            @NonNull HttpServletRequest request
    ) {
        log.info("REST request to get all Permissions [Search: '{}', Page: {}, Size: {}, SortBy: {}, Order: {}]",
                search != null ? search : "ALL", page, size, sortBy, order);

        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        var result = service.findAll(search, pageable);

        var response = StandardResponseDTO.<PaginationDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("Permissions list retrieved successfully")
                .status(200)
                .data(result)
                .path(request.getServletPath())
                .build();

        log.info("Successfully fetched {} permissions out of {} total elements [Status: 200]",
                result.getTotalElements(), result.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull PermissionDTO>> create(
            @NonNull @Valid @RequestBody PermissionRequestDTO dto,
            @NonNull HttpServletRequest request
    ) {
        log.info("REST request to CREATE Permission: '{}' [Path: {}]",
                dto.getName(), request.getServletPath());

        var result = service.create(dto);

        var response = StandardResponseDTO.<PermissionDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("Permission '" + result.getName() + "' created successfully")
                .status(201)
                .data(result)
                .path(request.getServletPath())
                .build();

        log.info("Permission successfully created with UUID: {} [Status: 201]", result.getUuid());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<@NonNull StandardResponseDTO<List<PermissionDTO>>> createMultiple(
            @RequestBody @Valid List<PermissionRequestDTO> requestDTOS,
            HttpServletRequest request
    ) {
        log.info("REST request to create multiple permissions [Count: {}]", requestDTOS.size());

        var results = service.createMultiple(requestDTOS);

        var response = StandardResponseDTO.<List<PermissionDTO>>builder()
                .timestamp(LocalDateTime.now())
                .message(results.size() + " Permissions created successfully")
                .status(201)
                .data(results)
                .path(request.getServletPath())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PatchMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull PermissionDTO>> update(
            @NonNull @PathVariable UUID uuid,
            @NonNull @Valid @RequestBody PermissionUpdateDTO dto,
            @NonNull HttpServletRequest request
    ) {
        log.info("REST request to PATCH Permission: {} [Data provided: {}]",
                uuid, dto.getName() != null ? "Name: " + dto.getName() : "Partial update (no name change)");

        var result = service.update(uuid, dto);

        var response = StandardResponseDTO.<PermissionDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("Permission updated successfully: " + result.getName())
                .status(200)
                .data(result)
                .path(request.getServletPath())
                .build();

        log.info("Successfully updated Permission with UUID: {} [Status: 200 OK]", uuid);

        return ResponseEntity.ok(response);
    }
}
