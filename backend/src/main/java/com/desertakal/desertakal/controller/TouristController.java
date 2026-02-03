package com.desertakal.desertakal.controller;

import com.desertakal.desertakal.exception.custom.FileUploadException;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.dto.responce.StandardResponseDTO;
import com.desertakal.desertakal.model.dto.tourist.TouristDTO;
import com.desertakal.desertakal.model.dto.tourist.TouristUpdateDTO;
import com.desertakal.desertakal.model.dto.user.UserFindDTO;
import com.desertakal.desertakal.model.dto.user.UserUpdateDTO;
import com.desertakal.desertakal.service.interfaces.TourService;
import com.desertakal.desertakal.service.interfaces.TouristService;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/tourists")
@RequiredArgsConstructor
@Slf4j
public class TouristController {
    private final TouristService service;
    private final TourService tourService;

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

        var response = StandardResponseDTO.<TouristDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("Profile updated successfully")
                .status(200)
                .data(result)
                .path(request.getServletPath())
                .build();

        return ResponseEntity.ok(response);
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

        var response = StandardResponseDTO.<TouristDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("")
                .status(200)
                .path(request.getServletPath())
                .data(result)
                .build();

        log.info("Tourist with UUID: {} has been successfully patched via {}", uuid, request.getServletPath());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{uuid}")
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

        var response = StandardResponseDTO.<PaginationDTO>builder()
                .timestamp(LocalDateTime.now())
                .message(message)
                .status(200)
                .path(request.getServletPath())
                .data(result)
                .build();

        log.info("Successfully returned {} tours for Tourist UUID: {} [Status: 200]",
                result.getTotalElements(), uuid);

        return ResponseEntity.ok(response);
    }

}
