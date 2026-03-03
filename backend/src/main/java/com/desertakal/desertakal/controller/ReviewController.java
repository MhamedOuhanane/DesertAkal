package com.desertakal.desertakal.controller;

import com.desertakal.desertakal.Security.user.CustomUserDetails;
import com.desertakal.desertakal.model.dto.responce.StandardResponseDTO;
import com.desertakal.desertakal.model.dto.review.ReviewCreateDTO;
import com.desertakal.desertakal.model.dto.review.ReviewDTO;
import com.desertakal.desertakal.service.interfaces.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Objects;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {
    private final ReviewService service;

    @PostMapping
    @PreAuthorize("hasRole('TOURIST')")
    @Operation(summary = "Create a review",
            description = "Tourist creates a review for a Tour or Guide")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull ReviewDTO>> create(
            @Valid @RequestBody ReviewCreateDTO dto,
            @AuthenticationPrincipal CustomUserDetails customUser,
            HttpServletRequest request) {

        log.info("REST request to create Review for {} {} by user: {}",
                dto.getReviewableType(), dto.getReviewableUuid(), customUser.getEmail());

        log.debug("Request details - IP: {}, Content-Type: {}",
                request.getRemoteAddr(), request.getContentType());

        ReviewDTO result = service.create(dto, customUser.getUuid());

        log.info("Review successfully created with UUID: {} for {} {}",
                result.getUuid(), dto.getReviewableType(), dto.getReviewableUuid());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(buildResponse("Review created successfully.", HttpStatus.CREATED, request, result));
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

    private boolean isAdmin (CustomUserDetails currentUser) {
        return currentUser.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));
    }
}
