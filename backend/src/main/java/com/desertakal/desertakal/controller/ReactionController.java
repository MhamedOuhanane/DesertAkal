package com.desertakal.desertakal.controller;

import com.desertakal.desertakal.Security.user.CustomUserDetails;
import com.desertakal.desertakal.model.dto.reaction.ReactionCreateDTO;
import com.desertakal.desertakal.model.dto.reaction.ReactionSummaryDTO;
import com.desertakal.desertakal.model.dto.reaction.ReactionToggleResponseDTO;
import com.desertakal.desertakal.model.dto.responce.StandardResponseDTO;
import com.desertakal.desertakal.service.interfaces.ReactionService;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/reactions")
@RequiredArgsConstructor
@Slf4j
public class ReactionController {
    private final ReactionService service;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<@NonNull StandardResponseDTO<ReactionToggleResponseDTO>> toggle(
            @Valid @RequestBody ReactionCreateDTO dto,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest request
    ) {

        log.info("REST request to toggle reaction on article: {} | User: {}",
                dto.getArticleUuid(), currentUser.getEmail());

        ReactionToggleResponseDTO result = service.toggle(dto, currentUser.getUuid());

        String message = getActionMessage(result.getAction());

        HttpStatus status = result.getAction().equals("ADDED")
                ? HttpStatus.CREATED
                : HttpStatus.OK;

        return ResponseEntity.ok(
                buildResponse(message, status, request, result));
    }

    @GetMapping("/articles/{articleUuid}/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<@NonNull StandardResponseDTO<ReactionSummaryDTO>> getSummary(
            @PathVariable UUID articleUuid,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest request) {

       log.info("REST request to get reaction summary for article: {} | User: {}",
                articleUuid, (currentUser != null ? currentUser.getEmail() : "Anonymous"));

        assert currentUser != null;
        ReactionSummaryDTO result = service.getSummary(articleUuid, currentUser.getUuid());

        return ResponseEntity.ok(
                buildResponse("Reaction summary retrieved successfully.", HttpStatus.OK, request, result));
    }

    private String getActionMessage(String action) {
        return switch (action) {
            case "ADDED"   -> "Reaction added successfully.";
            case "CHANGED" -> "Reaction changed successfully.";
            case "REMOVED" -> "Reaction removed successfully.";
            default        -> "Reaction processed successfully.";
        };
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
