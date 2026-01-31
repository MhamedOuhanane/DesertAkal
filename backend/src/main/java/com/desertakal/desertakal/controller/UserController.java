package com.desertakal.desertakal.controller;

import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.dto.responce.StandardResponseDTO;
import com.desertakal.desertakal.model.dto.user.UserFindDTO;
import com.desertakal.desertakal.model.dto.user.UserStatusUpdateDTO;
import com.desertakal.desertakal.model.dto.user.UserUpdateDTO;
import com.desertakal.desertakal.model.enums.UserStatus;
import com.desertakal.desertakal.service.interfaces.UserService;
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
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

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

        var response = StandardResponseDTO.<PaginationDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("Users retrieved successfully")
                .status(200)
                .path(request.getServletPath())
                .data(result)
                .build();

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

        var response = StandardResponseDTO.<UserFindDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("User details retrieved successfully")
                .status(200)
                .path(request.getServletPath())
                .data(result)
                .build();

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

        var response = StandardResponseDTO.<UserFindDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("")
                .status(200)
                .path(request.getServletPath())
                .data(result)
                .build();

        log.info("User with UUID: {} has been successfully patched via {}", uuid, request.getServletPath());

        return ResponseEntity.ok(response);
    }


    @PatchMapping("/{uuid}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull UserFindDTO>> updateStatus(
            @NonNull @PathVariable UUID uuid,
            @NonNull @Valid UserStatusUpdateDTO dto,
            @NonNull HttpServletRequest request
    ) {
        log.info("REST request to update status of user {} to {}", uuid, dto.getStatus());

        var result = userService.updateStatus(uuid, dto.getStatus());

        return ResponseEntity.ok(
                StandardResponseDTO.<UserFindDTO>builder()
                        .timestamp(LocalDateTime.now())
                        .message("User status updated successfully")
                        .status(200)
                        .path(request.getServletPath())
                        .data(result)
                        .build()
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

        return ResponseEntity.ok(
                StandardResponseDTO.<UserFindDTO>builder()
                        .timestamp(LocalDateTime.now())
                        .message("User photo updated successfully")
                        .status(200)
                        .path(request.getServletPath())
                        .data(result)
                        .build()
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

        var response = StandardResponseDTO.<Void>builder()
                .timestamp(LocalDateTime.now())
                .message("User has been successfully deleted")
                .status(200)
                .path(request.getServletPath())
                .data(null)
                .build();


        log.info("User with UUID: {} deleted successfully", uuid);

        return ResponseEntity.ok(response);
    }
}
