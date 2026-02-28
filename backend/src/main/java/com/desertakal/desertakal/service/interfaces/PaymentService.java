package com.desertakal.desertakal.service.interfaces;

import com.desertakal.desertakal.model.dto.payment.PaymentCreateDTO;
import com.desertakal.desertakal.model.dto.payment.PaymentFindDTO;
import com.desertakal.desertakal.model.dto.payment.PaymentResponseDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.enums.PaymentStatus;
import org.jspecify.annotations.NonNull;

import java.awt.print.Pageable;
import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentService {
    PaymentResponseDTO initiatePayment(@NonNull PaymentCreateDTO dto, @NonNull UUID touristUuid);
    PaymentFindDTO capturePayment(@NonNull String paypalOrderId, @NonNull UUID touristUuid);
    PaymentFindDTO cancelPayment(@NonNull UUID paymentUuid, @NonNull UUID touristUuid);
    PaymentFindDTO refundPayment(@NonNull UUID paymentUuid, @NonNull UUID adminUuid);
    PaymentFindDTO partialRefundPayment(@NonNull UUID paymentUuid, @NonNull BigDecimal amount, @NonNull UUID adminUuid);
    PaymentFindDTO getPayment(@NonNull UUID paymentUuid);
    PaginationDTO getPaymentsByReservation(@NonNull UUID reservationUuid, @NonNull Pageable pageable);
    PaginationDTO getPaymentsByTourist(@NonNull UUID touristUuid, PaymentStatus status, @NonNull Pageable pageable);
    PaginationDTO getAllPayments(PaymentStatus status, @NonNull Pageable pageable);
}
