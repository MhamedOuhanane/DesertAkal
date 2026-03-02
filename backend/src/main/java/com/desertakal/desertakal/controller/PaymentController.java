package com.desertakal.desertakal.controller;

import com.desertakal.desertakal.Security.user.CustomUserDetails;
import com.desertakal.desertakal.model.dto.payment.*;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.dto.responce.StandardResponseDTO;
import com.desertakal.desertakal.model.enums.PaymentMethod;
import com.desertakal.desertakal.model.enums.PaymentStatus;
import com.desertakal.desertakal.model.enums.PaymentType;
import com.desertakal.desertakal.service.interfaces.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasRole('TOURIST')")
    public ResponseEntity<@NonNull StandardResponseDTO<PaymentResponseDTO>> initiate(
            @Valid @RequestBody PaymentCreateDTO dto,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @NonNull HttpServletRequest request
    ) {

        log.info("REST request to initiate {} payment for reservation {} by user {}",
                dto.getMethod(), dto.getReservationUuid(), currentUser.getUuid());

        PaymentResponseDTO result = paymentService.initiatePayment(dto, currentUser.getUuid());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                buildResponse("Payment initiated successfully", HttpStatus.CREATED, request, result)
        );
    }

    @PostMapping("/capture")
    @PreAuthorize("hasRole('TOURIST')")
    public ResponseEntity<@NonNull StandardResponseDTO<PaymentFindDTO>> capture(
            @RequestParam String orderId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @NonNull HttpServletRequest request
    ) {

        log.info("REST request to capture payment for orderId: {} by user: {}", orderId, currentUser.getUuid());

        PaymentFindDTO result = paymentService.capturePayment(orderId, currentUser.getUuid());

        return ResponseEntity.ok(buildResponse("Payment captured successfully", HttpStatus.OK, request, result));
    }

    @PostMapping("/{uuid}/cancel")
    @PreAuthorize("hasRole('TOURIST')")
    public ResponseEntity<@NonNull StandardResponseDTO<PaymentFindDTO>> cancel(
            @PathVariable UUID uuid,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @NonNull HttpServletRequest request
    ) {

        log.info("REST request to cancel pending payment: {} by user: {}", uuid, currentUser.getUuid());

        PaymentFindDTO result = paymentService.cancelPayment(uuid, currentUser.getUuid());

        return ResponseEntity.ok(buildResponse("Payment cancelled successfully", HttpStatus.OK, request, result));
    }

    @PostMapping("/{uuid}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<@NonNull StandardResponseDTO<PaymentFindDTO>> refund(
            @PathVariable UUID uuid,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @NonNull HttpServletRequest request) {

        log.warn("ADMIN ACTION: Full refund for payment: {} by admin: {}", uuid, currentUser.getUuid());

        PaymentFindDTO result = paymentService.refundPayment(uuid, currentUser.getUuid());

        return ResponseEntity.ok(buildResponse("Full refund processed successfully", HttpStatus.OK, request, result));
    }

    @PostMapping("/{uuid}/refund/partial")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<@NonNull StandardResponseDTO<PaymentFindDTO>> partialRefund(
            @PathVariable UUID uuid,
            @Valid @RequestBody RefundRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @NonNull HttpServletRequest request) {

        log.warn("ADMIN ACTION: Partial refund (${}) for payment: {} by admin: {}",
                dto.getAmount(), uuid, currentUser.getUuid());

        PaymentFindDTO result = paymentService.partialRefundPayment(uuid, dto.getAmount(), currentUser.getUuid());

        return ResponseEntity.ok(buildResponse("Partial refund processed successfully", HttpStatus.OK, request, result));
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