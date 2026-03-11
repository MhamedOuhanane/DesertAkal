package com.desertakal.desertakal.controller;

import com.desertakal.desertakal.model.dto.admin.AdminDashboardDTO;
import com.desertakal.desertakal.model.dto.responce.StandardResponseDTO;
import com.desertakal.desertakal.service.interfaces.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<@NonNull StandardResponseDTO<AdminDashboardDTO>> getDashboardStats(
            HttpServletRequest request
    ) {

        log.info("REST request to get admin dashboard statistics for path: {}", request.getServletPath());

        AdminDashboardDTO stats = adminService.getGlobalDashboardStats();

        return ResponseEntity.ok(
                StandardResponseDTO.<AdminDashboardDTO>builder()
                        .timestamp(LocalDateTime.now())
                        .message("Dashboard statistics retrieved successfully.")
                        .status(HttpStatus.OK.value())
                        .path(request.getServletPath())
                        .data(stats)
                        .build()
        );
    }
}
