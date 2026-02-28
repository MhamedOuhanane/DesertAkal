package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.model.dto.payment.PaymentCreateDTO;
import com.desertakal.desertakal.model.dto.payment.PaymentFindDTO;
import com.desertakal.desertakal.model.dto.payment.PaymentResponseDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.enums.PaymentStatus;
import com.desertakal.desertakal.model.mapper.PaymentMapper;
import com.desertakal.desertakal.repository.PaymentRepository;
import com.desertakal.desertakal.service.interfaces.PayPalService;
import com.desertakal.desertakal.service.interfaces.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.print.Pageable;
import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository repository;
    private final PaymentMapper mapper;
    private final PayPalService payPalService;


    @Override
    public PaymentResponseDTO initiatePayment(@NonNull PaymentCreateDTO dto, @NonNull UUID touristUuid) {
        return null;
    }

    @Override
    public PaymentFindDTO capturePayment(@NonNull String paypalOrderId, @NonNull UUID touristUuid) {
        return null;
    }

    @Override
    public PaymentFindDTO cancelPayment(@NonNull UUID paymentUuid, @NonNull UUID touristUuid) {
        return null;
    }

    @Override
    public PaymentFindDTO refundPayment(@NonNull UUID paymentUuid, @NonNull UUID adminUuid) {
        return null;
    }

    @Override
    public PaymentFindDTO partialRefundPayment(@NonNull UUID paymentUuid, @NonNull BigDecimal amount, @NonNull UUID adminUuid) {
        return null;
    }

    @Override
    public PaymentFindDTO getPayment(@NonNull UUID paymentUuid) {
        return null;
    }

    @Override
    public PaginationDTO getPaymentsByReservation(@NonNull UUID reservationUuid, @NonNull Pageable pageable) {
        return null;
    }

    @Override
    public PaginationDTO getPaymentsByTourist(@NonNull UUID touristUuid, PaymentStatus status, @NonNull Pageable pageable) {
        return null;
    }

    @Override
    public PaginationDTO getAllPayments(PaymentStatus status, @NonNull Pageable pageable) {
        return null;
    }
}
