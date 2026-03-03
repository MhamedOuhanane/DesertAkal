package com.desertakal.desertakal.service.interfaces;

import com.desertakal.desertakal.model.entity.Payment;
import com.desertakal.desertakal.model.entity.Reservation;
import com.desertakal.desertakal.model.enums.PaymentMethod;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;

public interface PaymentGateway {
    String createOrder(@NonNull Payment payment, @NonNull Reservation reservation);
    String captureOrder(@NonNull String gatewayPaymentId);
    void refundPayment(@NonNull String gatewaySessionId, @NonNull BigDecimal amount);
    PaymentMethod getMethod();
}
