package com.desertakal.desertakal.controller;

import com.desertakal.desertakal.Security.user.CustomUserDetails;
import com.desertakal.desertakal.config.CookieConfig;
import com.desertakal.desertakal.model.dto.auth.EmailVerificationDTO;
import com.desertakal.desertakal.model.dto.auth.LoginRequestDTO;
import com.desertakal.desertakal.model.dto.auth.RegisterDTO;
import com.desertakal.desertakal.model.dto.refreshToken.RefreshTokenRequestDTO;
import com.desertakal.desertakal.service.interfaces.EmailVerificationTokenService;
import com.desertakal.desertakal.service.interfaces.RefreshTokenService;
import com.desertakal.desertakal.service.interfaces.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final UserService service;
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationTokenService emailVerificationTokenService;
    private final CookieConfig cookieConfig;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @NonNull @Valid @RequestBody RegisterDTO dto,
            @NonNull HttpServletRequest request
    ) {
        log.info("REST request to register new user: {} | IP: {}", dto.getEmail(), request.getRemoteAddr());
        service.register(dto);

        log.info("Registration successful for user: {}", dto.getEmail());

        return ResponseEntity.status(201).body(
                Map.of(
                        "timestamp", LocalDateTime.now().toString(),
                        "message", "Register successful. Please check your email for activation.",
                        "status", "201",
                        "path", request.getServletPath()
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @NonNull @Valid @RequestBody LoginRequestDTO dto,
            @NonNull HttpServletRequest request
    ) {
        log.info("REST request to login user: {} | IP: {} | Device: {}",
                dto.getUsername(),
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

        String userAgent = request.getHeader("User-Agent");
        String ipAddress = request.getRemoteAddr();

        var result = service.login(dto, ipAddress, userAgent);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", result.getRefreshToken())
                        .httpOnly(true)
                        .secure(cookieConfig.isSecure())
                        .path("/api/auth/refresh")
                        .maxAge(cookieConfig.getMaxAge())
                        .sameSite(cookieConfig.getSameSite())
                        .build();

        result.setRefreshToken(null);

        log.info("Login successful for user: {} | Path: {}", dto.getUsername(), request.getServletPath());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(
                    Map.of(
                            "timestamp", LocalDateTime.now().toString(),
                            "message", "Login successful!",
                            "status", 200,
                            "path", request.getServletPath(),
                            "data", result
                    )
                );
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @NonNull @CookieValue(name = "refreshToken") String token,
            @NonNull HttpServletRequest request
    ) {
        log.info("REST request to refresh token | IP: {} | Device: {}",
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

        var result = refreshTokenService.refresh(
                token,
                RefreshTokenRequestDTO.builder()
                        .userAgent(request.getHeader("User-Agent"))
                        .ipAddress(request.getRemoteAddr())
                        .deviceId(request.getHeader("X-Device-ID"))
                        .build()
        );

        ResponseCookie newCookie = ResponseCookie.from("refreshToken", result.getRefreshToken())
                        .httpOnly(true)
                        .secure(cookieConfig.isSecure())
                        .path("/api/auth/refresh")
                        .maxAge(cookieConfig.getMaxAge())
                        .sameSite(cookieConfig.getSameSite())
                        .build();

        result.setRefreshToken(null);

        log.info("Token refreshed successfully for user: {}", result.getUsername());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newCookie.toString())
                .body(
                        Map.of(
                                "timestamp", LocalDateTime.now().toString(),
                                "message", "Token refreshed successfully!",
                                "status", 200,
                                "path", request.getServletPath(),
                                "data", result
                        )
                );
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(
            @NonNull @Valid @RequestBody EmailVerificationDTO dto,
            @NonNull HttpServletRequest request
    ) {
        log.info("REST request to resend verification email to: {} | IP: {}", dto.getEmail(), request.getRemoteAddr());

        emailVerificationTokenService.createVerificationToken(dto.getEmail());

        return ResponseEntity.ok(
                Map.of(
                        "timestamp", LocalDateTime.now().toString(),
                        "message", "A new verification link has been sent to your email address.",
                        "status", "200",
                        "path", request.getServletPath()
                )
        );
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(
            @NonNull @RequestParam("token") String token,
            @NonNull HttpServletRequest request
    ) {
        log.info("Received email verification request for token: {} | IP: {}", token, request.getRemoteAddr());

        emailVerificationTokenService.confirmEmail(token);

        log.info("Email verified successfully for token: {}", token);

        return ResponseEntity.ok(
                Map.of(
                        "timestamp", LocalDateTime.now().toString(),
                        "message", "Your email has been verified successfully. You can now log in to your account.",
                        "status", "200",
                        "path", request.getServletPath()
                )
        );
    }

    @GetMapping("/sessions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMySessions(
            @NonNull @AuthenticationPrincipal CustomUserDetails userDetails,
            @NonNull HttpServletRequest request
    ) {
        log.info("REST request to fetch active sessions for user: {} | UUID: {} | IP: {}",
                userDetails.getEmail(), userDetails.getUuid(), request.getRemoteAddr());

        var activeSessions = refreshTokenService.getActiveSessions(userDetails.getUuid());

        log.info("Successfully retrieved {} active sessions for user: {}",
                activeSessions.size(), userDetails.getEmail());

        return ResponseEntity.ok(
                Map.of(
                        "timestamp", LocalDateTime.now().toString(),
                        "message", "Active sessions retrieved successfully.",
                        "status", 200,
                        "path", request.getServletPath(),
                        "data", activeSessions
                )
        );
    }

}
