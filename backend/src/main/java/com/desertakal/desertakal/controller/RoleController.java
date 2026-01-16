package com.desertakal.desertakal.controller;

import com.desertakal.desertakal.Security.user.CustomUserDetails;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.dto.responce.StandardResponseDTO;
import com.desertakal.desertakal.service.interfaces.PermissionService;
import com.desertakal.desertakal.service.interfaces.RoleService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Slf4j
public class RoleController {
    private final RoleService service;
    private final PermissionService permissionService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull PaginationDTO>> shows(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String order,
            @NonNull HttpServletRequest request
    ) {
        log.info("REST request to get a page of Roles [Page: {}, Size: {}] from path: {}",
                page, size, request.getServletPath());

        Pageable pageable = getPageable(page, size, sortBy, order);

        var result = service.findAll(search, pageable);

        var response = StandardResponseDTO.<PaginationDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("Roles retrieved successfully")
                .status(200)
                .data(result)
                .path(request.getServletPath())
                .build();

        log.info("REST response for Roles: {} elements found out of {} total [Status: 200]",
                result.getSize(), result.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<@NonNull StandardResponseDTO<@NonNull PaginationDTO>> showsPermissions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String roleName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String order,
            @NonNull @AuthenticationPrincipal CustomUserDetails userDetails,
            @NonNull HttpServletRequest request
    ) {
        if ((roleName == null || roleName.isBlank()) && !userDetails.getRoleName().equalsIgnoreCase("ADMIN")) {
            roleName = userDetails.getRoleName();
        }

        log.info("REST request to get a page of Permissions of Role {} [Page: {}, Size: {}] from path: {}",
                roleName, page, size, request.getServletPath());

        Pageable pageable = getPageable(page, size, sortBy, order);

        var result = permissionService.findByRole(roleName, search, pageable);

        var response = StandardResponseDTO.<PaginationDTO>builder()
                .timestamp(LocalDateTime.now())
                .message("Roles retrieved successfully")
                .status(200)
                .data(result)
                .path(request.getServletPath())
                .build();

        log.info("REST response for Permission of Role {}: {} elements found out of {} total [Status: 200]",
                roleName, result.getSize(), result.getTotalElements());

        return ResponseEntity.ok(response);
    }

    private Pageable getPageable(int page, int size, String sortBy, String order) {
        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        return PageRequest.of(page, size, sort);
    }
}
