package com.desertakal.desertakal.controller;

import com.desertakal.desertakal.Security.user.CustomUserDetails;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.dto.responce.StandardResponseDTO;
import com.desertakal.desertakal.model.dto.review.ReviewCreateDTO;
import com.desertakal.desertakal.model.dto.review.ReviewDTO;
import com.desertakal.desertakal.model.dto.review.ReviewUpdateDTO;
import com.desertakal.desertakal.model.enums.ReviewableType;
import com.desertakal.desertakal.service.interfaces.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService service;

    @PostMapping
    @PreAuthorize("hasRole('TOURIST')")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull ReviewDTO>> create(
            @Valid @RequestBody ReviewCreateDTO dto,
            @AuthenticationPrincipal CustomUserDetails customUser,
            HttpServletRequest request) {

        log.info("Creating review for {} {} | User: {}",
                dto.getReviewableType(), dto.getReviewableUuid(), customUser.getEmail());

        ReviewDTO result = service.create(dto, customUser.getUuid());

        log.info("Review created: UUID={}", result.getUuid());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(buildResponse("Review created successfully.", HttpStatus.CREATED, request, result));
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasRole('TOURIST')")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull ReviewDTO>> update(
            @PathVariable UUID uuid,
            @Valid @RequestBody ReviewUpdateDTO dto,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest request) {

        log.info("Updating review [{}] | User: {}", uuid, currentUser.getEmail());

        ReviewDTO result = service.update(uuid, dto, currentUser.getUuid());

        return ResponseEntity.ok(buildResponse("Review updated successfully.", HttpStatus.OK, request, result));
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('TOURIST', 'ADMIN')")
    public ResponseEntity<@NonNull StandardResponseDTO<Void>> delete(
            @PathVariable UUID uuid,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest request) {

        boolean isAdmin = isAdmin(currentUser);
        log.info("Deleting review [{}] | User: {} | Admin: {}", uuid, currentUser.getEmail(), isAdmin);

        service.delete(uuid, currentUser.getUuid(), isAdmin);

        return ResponseEntity.ok(buildResponse("Review deleted successfully.", HttpStatus.OK, request, null));
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull ReviewDTO>> get(
            @PathVariable UUID uuid,
            HttpServletRequest request) {

        log.debug("Fetching review details: {}", uuid);
        return ResponseEntity.ok(buildResponse("Review retrieved.", HttpStatus.OK, request, service.get(uuid)));
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull PaginationDTO>> all(
            @RequestParam(required = false) ReviewableType type,
            @RequestParam(required = false) BigDecimal minRating,
            Pageable pageable,
            HttpServletRequest request) {

        log.info("Fetching reviews list [Type: {}, minRating: {}]", type, minRating);
        PaginationDTO result = service.getAll(type, minRating, pageable);

        return ResponseEntity.ok(buildResponse("Reviews retrieved.", HttpStatus.OK, request, result));
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

    private boolean isAdmin(CustomUserDetails currentUser) {
        return currentUser.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));
    }
}