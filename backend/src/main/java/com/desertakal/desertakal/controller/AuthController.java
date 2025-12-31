package com.desertakal.desertakal.controller;

import com.desertakal.desertakal.model.dto.auth.EmailVerificationDTO;
import com.desertakal.desertakal.model.dto.auth.RegisterDTO;
import com.desertakal.desertakal.service.interfaces.EmailVerificationTokenService;
import com.desertakal.desertakal.service.interfaces.RefreshTokenService;
import com.desertakal.desertakal.service.interfaces.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final UserService service;
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationTokenService emailVerificationTokenService;

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
                        "message", "User registered successfully. Please check your email for activation.",
                        "status", "201",
                        "path", request.getServletPath()
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

}
