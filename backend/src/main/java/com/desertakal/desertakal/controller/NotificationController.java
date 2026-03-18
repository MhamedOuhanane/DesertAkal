package com.desertakal.desertakal.controller;

import com.desertakal.desertakal.Security.user.CustomUserDetails;
import com.desertakal.desertakal.model.dto.notif.NotificationFindDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.dto.responce.StandardResponseDTO;
import com.desertakal.desertakal.service.interfaces.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    private final NotificationService service;

    @GetMapping("/user/{userUuid}")
    @PreAuthorize("@ownerSecurityService.isOwner(#userUuid, authentication, false )")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull PaginationDTO>> shows(
            @PathVariable UUID userUuid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            @NonNull HttpServletRequest request
    ) {
        log.info("REST request to get all notifications for User: '{}' [Page: {}, Size: {}, SortBy: {}, Order: {}]",
                userUuid, page, size, sortBy, order);

        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        var result = service.findByUser(userUuid, pageable);

        String message = result.getTotalElements() > 0
                ? String.format("Successfully retrieved %d notification(s).", result.getTotalElements())
                : "No notifications found for this user.";

        var response = StandardResponseDTO.<PaginationDTO>builder()
                .timestamp(LocalDateTime.now())
                .message(message)
                .status(200)
                .data(result)
                .path(request.getServletPath())
                .build();

        log.info("Response sent: {} notifications found for User: {} [Path: {}]",
                result.getTotalElements(), userUuid, request.getServletPath());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull NotificationFindDTO>> show(
            @PathVariable UUID uuid,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ) {
        log.info("REST request to get Notification by UUID: {} [Path: {}]", uuid, request.getServletPath());

        var result = service.find(uuid, userDetails.getUuid(), isAdmin(userDetails));

        var response = StandardResponseDTO.<NotificationFindDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("Notification details retrieved successfully")
                .status(200)
                .path(request.getServletPath())
                .data(result)
                .build();

        log.info("Successfully retrieved Notification details for UUID: {}", uuid);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<@NonNull StandardResponseDTO<Void>> delete(
            @PathVariable UUID uuid,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ) {
        log.info("REST request to DELETE Notification with UUID: {} [Requested by Path: {}]",
                uuid, request.getServletPath());

        service.delete(uuid, userDetails.getUuid(), isAdmin(userDetails));

        var response = StandardResponseDTO.<Void>builder()
                .timestamp(LocalDateTime.now())
                .status(200)
                .message("Notification has been successfully deleted")
                .path(request.getServletPath())
                .build();

        log.info("Successfully deleted Notification with UUID: {} [Status: 200 OK]", uuid);

        return ResponseEntity.ok(response);
    }

    private boolean isAdmin(CustomUserDetails currentUser) {
        return currentUser.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));
    }
}