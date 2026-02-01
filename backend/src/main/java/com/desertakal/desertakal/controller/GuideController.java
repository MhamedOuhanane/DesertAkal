package com.desertakal.desertakal.controller;

import com.desertakal.desertakal.model.dto.guide.GuideCreateDTO;
import com.desertakal.desertakal.model.dto.guide.GuideFindDTO;
import com.desertakal.desertakal.model.dto.responce.StandardResponseDTO;
import com.desertakal.desertakal.model.dto.user.UserFindDTO;
import com.desertakal.desertakal.service.interfaces.GuideService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
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
    @PreAuthorize("@ownerSecurityService.isOwner(#uuid, authentication, true)")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull UserFindDTO>> find(
            @NonNull @PathVariable UUID uuid,
            @NonNull HttpServletRequest request
    ) {
        log.info("REST request to get Guide by UUID: {} [Path: {}]", uuid, request.getServletPath());

        var result = service.find(uuid);

        var response = StandardResponseDTO.<UserFindDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("Guide details retrieved successfully")
                .status(200)
                .path(request.getServletPath())
                .data(result)
                .build();

        log.info("Successfully retrieved guide details for UUID: {}", uuid);

        return ResponseEntity.ok(response);
    }
}
