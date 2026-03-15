package com.desertakal.desertakal.controller;

import com.desertakal.desertakal.Security.user.CustomUserDetails;
import com.desertakal.desertakal.model.dto.article.ArticleCreateDTO;
import com.desertakal.desertakal.model.dto.article.ArticleDTO;
import com.desertakal.desertakal.model.dto.article.ArticleUpdateDTO;
import com.desertakal.desertakal.model.dto.comment.CommentDTO;
import com.desertakal.desertakal.model.dto.reaction.ReactionDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.dto.responce.StandardResponseDTO;
import com.desertakal.desertakal.model.enums.ReactionEnum;
import com.desertakal.desertakal.service.interfaces.ArticleService;
import com.desertakal.desertakal.service.interfaces.ReactionService;
import com.desertakal.desertakal.service.interfaces.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Slf4j
public class ArticleController {
    private final ArticleService service;
    private final ReactionService reactionService;
    private final CommentService commentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull ArticleDTO>> create(
            @Valid @RequestPart("article") ArticleCreateDTO dto,
            @RequestPart("coverImage") MultipartFile coverImage,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest request
    ) {

        log.info("REST request to create Article by user: {} [File: {}, Size: {} bytes]",
                currentUser.getEmail(), coverImage.getOriginalFilename(), coverImage.getSize());

        String contentSnippet = dto.getContent().length() > 50
                ? dto.getContent().substring(0, 50) + "..."
                : dto.getContent();
        log.debug("Article content snippet: {}", contentSnippet);

        ArticleDTO result = service.create(dto, coverImage, currentUser.getUuid());

        log.info("Article created successfully with UUID: {} for user: {}",
                result.getUuid(), currentUser.getEmail());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(buildResponse(
                        "Article created successfully",
                        HttpStatus.CREATED,
                        request, result)
                );
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull PaginationDTO>> shows(
            @RequestParam(required = false) String search,
            Pageable pageable,
            HttpServletRequest request
    ) {
        log.info("REST request to get all articles : Page: {} | Size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        PaginationDTO result = service.getAll(search, pageable);

        log.info("Successfully retrieved {} articles",
                result.getTotalElements());

        return ResponseEntity.ok(buildResponse(
                "Articles retrieved successfully",
                HttpStatus.OK,
                request, result
        ));
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<@NonNull StandardResponseDTO<ArticleDTO>> get(
            @PathVariable UUID uuid,
            HttpServletRequest request
    ) {
        log.info("REST request to fetch article: {} | Path: {}", uuid, request.getServletPath());

        ArticleDTO result = service.get(uuid);

        return ResponseEntity.ok(buildResponse("Article retrieved successfully", HttpStatus.OK, request, result));
    }

    @PutMapping(value = "/{uuid}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull ArticleDTO>> update(
            @PathVariable UUID uuid,
            @Valid @RequestPart("article") ArticleUpdateDTO dto,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest request
    ) {

        log.info("REST request to update Article [{}]. User: {} | New Image: {}",
                uuid, currentUser.getUuid(), (coverImage != null && !coverImage.isEmpty()));

        if (coverImage != null) {
            log.debug("New cover image metadata - Name: {}, Size: {} bytes",
                    coverImage.getOriginalFilename(), coverImage.getSize());
        }

        ArticleDTO result = service.update(uuid, dto, coverImage, currentUser.getUuid());

        log.info("Article [{}] successfully updated.", uuid);

        return ResponseEntity.ok(buildResponse(
                "Article updated successfully",
                HttpStatus.OK,
                request, result
                )
        );
    }

    @PatchMapping(value = "/{uuid}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull ArticleDTO>> updateCoverImage(
            @PathVariable UUID uuid,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest request) {

        log.info("REST request to update cover image for Article [{}]. User: {}", uuid, currentUser.getUuid());

        if (coverImage.isEmpty()) {
            log.warn("Empty file uploaded for Article [{}] image update.", uuid);
        }

        ArticleDTO result = service.update(uuid, null, coverImage, currentUser.getUuid());

        log.info("Cover image for Article [{}] updated successfully.", uuid);

        return ResponseEntity.ok(buildResponse(
                "Cover image updated successfully",
                HttpStatus.OK,
                request, result
            )
        );
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<@NonNull StandardResponseDTO<Void>> delete(
            @PathVariable UUID uuid,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest request
    ) {
        boolean isAdmin = isAdmin(currentUser);

        log.info("REST request to delete Article [{}]. User: {} | Role: {}",
                uuid, currentUser.getUuid(), isAdmin ? "ADMIN" : "GUIDE/AUTHOR");

        service.delete(uuid, currentUser.getUuid(), isAdmin);

        log.info("Article [{}] successfully deleted and related data cascaded.", uuid);

        return ResponseEntity.ok(buildResponse(
                "Article deleted successfully",
                HttpStatus.OK,
                request, null)
        );
    }

    @GetMapping("/{uuid}/reactions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<@NonNull StandardResponseDTO<PaginationDTO>> getReactions(
            @PathVariable UUID uuid,
            @RequestParam(required = false) ReactionEnum type,
            Pageable pageable,
            HttpServletRequest request) {

        log.info("REST request to list reactions for article: {} | Page: {} | Size: {}",
                uuid, pageable.getPageNumber(), pageable.getPageSize());

        PaginationDTO result = reactionService.getByArticle(uuid, type, pageable);

        log.info("Successfully retrieved {} reactions for article: {}",
                result.getTotalElements(), uuid);

        return ResponseEntity.ok(
                buildResponse("Reactions retrieved successfully.", HttpStatus.OK, request, result));
    }

    @GetMapping("/{uuid}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<@NonNull StandardResponseDTO<PaginationDTO>> getComments(
            @PathVariable UUID uuid,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable,
            HttpServletRequest request) {

        log.info("REST request to get comments for article: {} | Page: {}", uuid, pageable.getPageNumber());

        PaginationDTO result = commentService.getByArticle(uuid, pageable);

        return ResponseEntity.ok(buildResponse("Comments retrieved successfully for article", HttpStatus.OK, request, result));
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
