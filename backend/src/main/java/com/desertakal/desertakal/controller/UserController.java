package com.desertakal.desertakal.controller;

import com.desertakal.desertakal.model.dto.comment.CommentDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.dto.responce.StandardResponseDTO;
import com.desertakal.desertakal.model.dto.user.UserFindDTO;
import com.desertakal.desertakal.model.dto.user.UserStatusUpdateDTO;
import com.desertakal.desertakal.model.dto.user.UserUpdateDTO;
import com.desertakal.desertakal.model.enums.UserStatus;
import com.desertakal.desertakal.service.interfaces.ArticleService;
import com.desertakal.desertakal.service.interfaces.CommentService;
import com.desertakal.desertakal.service.interfaces.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    private final ArticleService articleService;
    private final CommentService commentService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull PaginationDTO>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) String roleName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastLoginAt") String sortBy,
            @RequestParam(defaultValue = "asc") String order,
            @NonNull HttpServletRequest request
    ) {
        log.info("REST request to get a page of Users [Page: {}, Size: {}] from path: {}",
                page, size, request.getServletPath());

        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        var result = userService.findAll(search, status, roleName, pageable);

        var response = buildResponse(
                "Users retrieved successfully",
                request, result);

        log.info("Successfully processed users request for path: {}", request.getServletPath());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("@ownerSecurityService.isOwner(#uuid, authentication, true)")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull UserFindDTO>> getUser(
            @NonNull @PathVariable UUID uuid,
            @NonNull HttpServletRequest request
    ) {
        log.info("REST request to get User by UUID: {} [Path: {}]", uuid, request.getServletPath());

        var result = userService.find(uuid);

        var response = buildResponse(
                "User details retrieved successfully",
                request, result);

        log.info("Successfully retrieved user details for UUID: {}", uuid);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{uuid}")
    @PreAuthorize("@ownerSecurityService.isOwner(#uuid, authentication, true)")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull UserFindDTO>> updateUser(
            @NonNull @PathVariable UUID uuid,
            @NonNull @Valid @RequestBody UserUpdateDTO dto,
            @NonNull HttpServletRequest request
    ) {
        log.info("REST request to patch User : {} [Path: {}]", uuid, request.getServletPath());

        log.debug("Update payload for user {}: {}", uuid, dto);

        var result = userService.update(uuid, dto);

        var response = buildResponse(
                "User info updated successfully",
                request, result);

        log.info("User with UUID: {} has been successfully patched via {}", uuid, request.getServletPath());

        return ResponseEntity.ok(response);
    }


    @PatchMapping("/{uuid}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull UserFindDTO>> updateStatus(
            @NonNull @PathVariable UUID uuid,
            @NonNull @Valid @RequestBody UserStatusUpdateDTO dto,
            @NonNull HttpServletRequest request
    ) {
        log.info("REST request to update status of user {} to {}", uuid, dto.getStatus());

        var result = userService.updateStatus(uuid, dto.getStatus());

        return ResponseEntity.ok(buildResponse(
                "User status updated successfully",
                request, result)
        );
    }

    @PatchMapping("/{uuid}/photo")
    @PreAuthorize("@ownerSecurityService.isOwner(#uuid, authentication, true)")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull UserFindDTO>> updatePhoto(
            @NonNull @PathVariable UUID uuid,
            @NonNull @RequestParam(value = "photo") MultipartFile photo,
            @NonNull HttpServletRequest request
    ) {
        log.info("REST request to update Photo of user {}", uuid);

        var result = userService.updatePhoto(uuid, photo);

        return ResponseEntity.ok(buildResponse(
                "User photo updated successfully",
                request, result)
        );
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<@NonNull StandardResponseDTO<Void>> delete(
            @NonNull @PathVariable UUID uuid,
            @NonNull HttpServletRequest request
    ) {
        log.info("REST request to delete User : {} [Path: {}]", uuid, request.getServletPath());

        userService.delete(uuid);


        log.info("User with UUID: {} deleted successfully", uuid);

        return ResponseEntity.ok(buildResponse(
                "User has been successfully deleted",
                request, null)
        );
    }

    @GetMapping("/{uuid}/articles")
    @PreAuthorize("@ownerSecurityService.isOwner(#uuid, authentication, true )")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull PaginationDTO>> getArticles(
            @PathVariable UUID uuid,
            Pageable pageable,
            HttpServletRequest request
    ) {
        log.info("REST request to get articles for user: {} | Page: {} | Size: {}",
                uuid, pageable.getPageNumber(), pageable.getPageSize());

        PaginationDTO result = articleService.getByUser(uuid, pageable);

        log.info("Successfully retrieved {} articles for user: {}",
                result.getTotalElements(), uuid);

        return ResponseEntity.ok(buildResponse(
                "Articles retrieved successfully",
                request, result
        ));
    }

    @GetMapping("/{uuid}/comments")
    @PreAuthorize("@ownerSecurityService.isOwner(#uuid, authentication, true )")
    public ResponseEntity<@NonNull StandardResponseDTO<PaginationDTO>> getComments(
            @PathVariable UUID uuid,
            Pageable pageable,
            HttpServletRequest request
    ) {
        log.info("REST request to fetch comment by user: {} | Path: {}", uuid, request.getServletPath());

        PaginationDTO result = commentService.getByUser(uuid, pageable);

        return ResponseEntity.ok(buildResponse("Comment retrieved successfully", request, result));
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
}
