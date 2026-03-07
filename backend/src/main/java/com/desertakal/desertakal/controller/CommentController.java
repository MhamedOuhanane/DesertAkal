package com.desertakal.desertakal.controller;

import com.desertakal.desertakal.Security.user.CustomUserDetails;
import com.desertakal.desertakal.model.dto.comment.CommentCreateDTO;
import com.desertakal.desertakal.model.dto.comment.CommentDTO;
import com.desertakal.desertakal.model.dto.comment.CommentUpdateDTO;
import com.desertakal.desertakal.model.dto.responce.StandardResponseDTO;
import com.desertakal.desertakal.service.interfaces.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentController {
    private final CommentService service;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull CommentDTO>> create(
            @Valid @RequestBody CommentCreateDTO dto,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest request
    ) {
        log.info("REST request to create comment | User: {} | Article: {} | Path: {}",
                currentUser.getUuid(), dto.getArticleUuid(), request.getServletPath());

        CommentDTO result = service.create(dto, currentUser.getUuid());

        log.info("Comment created successfully | UUID: {}", result.getUuid());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(buildResponse("Comment created successfully", HttpStatus.CREATED, request, result));
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull CommentDTO>> update(
            @PathVariable UUID uuid,
            @Valid @RequestBody CommentUpdateDTO dto,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest request
    ) {

        log.info("REST request to update comment: {} | User: {} | Path: {}",
                uuid, currentUser.getUuid(), request.getServletPath());

        CommentDTO result = service.update(uuid, dto, currentUser.getUuid());

        return ResponseEntity.ok(buildResponse("Comment updated successfully", HttpStatus.OK, request, result));
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<@NonNull StandardResponseDTO<Void>> delete(
            @PathVariable UUID uuid,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest request
    ) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));

        log.warn("REST request to DELETE comment: {} | User: {} | IsAdmin: {}",
                uuid, currentUser.getUuid(), isAdmin);

        service.delete(uuid, currentUser.getUuid(), isAdmin);

        return ResponseEntity.ok(buildResponse("Comment deleted successfully", HttpStatus.OK, request, null));
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<@NonNull StandardResponseDTO<CommentDTO>> get(
            @PathVariable UUID uuid,
            HttpServletRequest request
    ) {
        log.info("REST request to fetch comment: {} | Path: {}", uuid, request.getServletPath());

        CommentDTO result = service.getByUuid(uuid);

        return ResponseEntity.ok(buildResponse("Comment retrieved successfully", HttpStatus.OK, request, result));
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
}
