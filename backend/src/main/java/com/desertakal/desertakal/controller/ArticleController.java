package com.desertakal.desertakal.controller;

import com.desertakal.desertakal.Security.user.CustomUserDetails;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.dto.responce.StandardResponseDTO;
import com.desertakal.desertakal.service.interfaces.ArticleService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Objects;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Slf4j
public class ArticleController {
    private final ArticleService service;

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
                request, result
        ));
    }

    private <T> StandardResponseDTO<T> buildResponse(String message, HttpServletRequest request, T data) {
        return StandardResponseDTO.<T>builder()
                .timestamp(LocalDateTime.now())
                .message(message)
                .status(HttpStatus.OK.value())
                .path(request.getServletPath())
                .data(data)
                .build();
    }

    private boolean isAdmin(CustomUserDetails currentUser) {
        return currentUser.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));
    }
}
