package com.desertakal.desertakal.controller;

import com.desertakal.desertakal.model.dto.responce.StandardResponseDTO;
import com.desertakal.desertakal.model.dto.tourist.TouristDTO;
import com.desertakal.desertakal.service.interfaces.TouristService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/tourists")
@RequiredArgsConstructor
@Slf4j
public class TouristController {
    private final TouristService service;

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

        var response = StandardResponseDTO.<TouristDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("Tourist details retrieved successfully")
                .status(200)
                .path(request.getServletPath())
                .data(result)
                .build();

        log.info("Successfully retrieved tourist for UUID: {}", uuid);

        return ResponseEntity.ok(response);
    }

}
