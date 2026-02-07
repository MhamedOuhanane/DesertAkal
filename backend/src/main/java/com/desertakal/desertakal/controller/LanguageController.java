package com.desertakal.desertakal.controller;

import com.desertakal.desertakal.model.dto.language.LanguageCreateDTO;
import com.desertakal.desertakal.model.dto.language.LanguageDTO;
import com.desertakal.desertakal.model.dto.language.LanguageUpdateDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.dto.responce.StandardResponseDTO;
import com.desertakal.desertakal.service.interfaces.LanguageService;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/languages")
@RequiredArgsConstructor
@Slf4j
public class LanguageController {
    private final LanguageService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull PaginationDTO>> shows(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String order,
            HttpServletRequest request
    ) {
        log.info("REST request to get all Language [Search: '{}', Page: {}, Size: {}, SortBy: {}, Order: {}]",
                search != null ? search : "ALL", page, size, sortBy, order);

        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        var result = service.findAll(search, pageable);

        var response = StandardResponseDTO.<PaginationDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("Language list retrieved successfully")
                .status(200)
                .data(result)
                .path(request.getServletPath())
                .build();

        log.info("Successfully fetched {} Language out of {} total elements [Status: 200]",
                result.getTotalElements(), result.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("@ownerSecurityService.isOwner(#uuid, authentication, true)")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull LanguageDTO>> show(
            @PathVariable UUID uuid,
            HttpServletRequest request
    ) {
        log.info("REST request to get Language by UUID: {} [Path: {}]", uuid, request.getServletPath());

        var result = service.find(uuid);

        var response = StandardResponseDTO.<LanguageDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("Language details retrieved successfully")
                .status(200)
                .path(request.getServletPath())
                .data(result)
                .build();

        log.info("Successfully retrieved Language details for UUID: {}", uuid);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull LanguageDTO>> create(
            @Valid @RequestBody LanguageCreateDTO dto,
            HttpServletRequest request
    ) {
        log.info("REST request to CREATE Language: '{}' [Path: {}]",
                dto.getName(), request.getServletPath());

        var result = service.create(dto);

        var response = StandardResponseDTO.<LanguageDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("Language '" + result.getName() + "' created successfully")
                .status(201)
                .data(result)
                .path(request.getServletPath())
                .build();

        log.info("Language successfully created with UUID: {} [Status: 201]", result.getUuid());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull LanguageDTO>> update(
            @PathVariable UUID uuid,
            @Valid @RequestBody LanguageUpdateDTO dto,
            HttpServletRequest request
    ) {
        log.info("REST request to PATCH Language: {} [Data provided: {}]",
                uuid, dto.getName() != null ? "Name: " + dto.getName() : "Partial update (no name change)");

        var result = service.update(uuid, dto);

        var response = StandardResponseDTO.<LanguageDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("Language updated successfully: " + result.getName())
                .status(200)
                .data(result)
                .path(request.getServletPath())
                .build();

        log.info("Successfully updated Language with UUID: {} [Status: 200 OK]", uuid);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<@NonNull StandardResponseDTO<Void>> delete(
            @PathVariable UUID uuid,
            HttpServletRequest request
    ) {
        log.info("REST request to DELETE Language with UUID: {} [Requested by Path: {}]",
                uuid, request.getServletPath());

        service.delete(uuid);

        var response = StandardResponseDTO.<Void>builder()
                .timestamp(LocalDateTime.now())
                .status(200)
                .message("Language has been successfully deleted")
                .path(request.getServletPath())
                .build();

        log.info("Successfully deleted Language with UUID: {} [Status: 200 OK]", uuid);

        return ResponseEntity.ok(response);
    }
}
