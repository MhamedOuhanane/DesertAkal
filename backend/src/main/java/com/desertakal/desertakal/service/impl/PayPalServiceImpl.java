package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.config.brand.BrandInfo;
import com.desertakal.desertakal.exception.custom.PaymentException;
import com.desertakal.desertakal.model.entity.Payment;
import com.desertakal.desertakal.model.entity.Reservation;
import com.desertakal.desertakal.model.enums.PaymentMethod;
import com.desertakal.desertakal.repository.PaymentRepository;
import com.desertakal.desertakal.service.interfaces.PayPalService;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import com.paypal.payments.CapturesRefundRequest;
import com.paypal.payments.RefundRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayPalServiceImpl implements PayPalService {

    private final PayPalHttpClient payPalClient;
    private final PaymentRepository paymentRepository;
    private final BrandInfo brand;

@Override
    public PaymentMethod getMethod() {
        return PaymentMethod.PAYPAL;
    }

    @Override
    public String createOrder(@NonNull Payment payment, @NonNull Reservation reservation) {
        try {
            String tourName =
                    reservation.getTour() != null
                            ? reservation.getTour().getTitle()
                            : "Desert Tour";

            OrderRequest orderRequest = new OrderRequest();
            orderRequest.checkoutPaymentIntent("CAPTURE");

            ApplicationContext context =
                    new ApplicationContext()
                            .brandName(BrandInfo.COMPANY_NAME)
                            .landingPage("BILLING")
                            .userAction("PAY_NOW")
                            .returnUrl(brand.getFrontendUrl()
                                    + "/payment/success?order_id=" + payment.getUuid())
                            .cancelUrl(brand.getFrontendUrl()
                                    + "/payment/cancel?payment_id=" + payment.getUuid());

            orderRequest.applicationContext(context);

            PurchaseUnitRequest purchaseUnit =
                    new PurchaseUnitRequest()
                            .referenceId(
                                    payment.getUuid().toString())
                            .description(String.format("%s - %d guests", tourName, reservation.getNumberPeople()))
                            .customId(payment.getUuid().toString())
                            .amountWithBreakdown(
                                    new AmountWithBreakdown()
                                            .currencyCode("EUR")
                                            .value(payment.getAmount().toPlainString())
                            );

            orderRequest.purchaseUnits(List.of(purchaseUnit));

            OrdersCreateRequest request = new OrdersCreateRequest();
            request.requestBody(orderRequest);

            HttpResponse<Order> response = payPalClient.execute(request);
            Order order = response.result();

            payment.setGatewayPaymentId(order.id());
            paymentRepository.save(payment);

            log.info("PayPal order created: {} for payment: {}", order.id(), payment.getUuid());

            return order.links().stream()
                    .filter(link ->
                            "approve".equals(link.rel()))
                    .findFirst()
                    .map(LinkDescription::href)
                    .orElseThrow(() ->
                            new PaymentException("PayPal approval URL not found"));

        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("PayPal order creation failed: {}", e.getMessage());
            throw new PaymentException("Failed to create PayPal order: " + e.getMessage());
        }
    }

    @Override
    public String captureOrder(@NonNull String gatewayPaymentId) {
        try {
            OrdersCaptureRequest request = new OrdersCaptureRequest(gatewayPaymentId);

            HttpResponse<Order> response =payPalClient.execute(request);
            Order order = response.result();

            if (!"COMPLETED".equals(order.status())) {
                throw new PaymentException("PayPal capture not completed. Status: " + order.status());
            }

            String captureId = order.purchaseUnits().get(0)
                            .payments()
                            .captures().get(0)
                            .id();

            log.info("PayPal captured. Order: {}, Capture: {}", gatewayPaymentId, captureId);

            return captureId;

        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error(
                    "PayPal capture failed: {}",
                    e.getMessage());
            throw new PaymentException(
                    "Failed to capture payment: "
                            + e.getMessage());
        }
    }

    @Override
    public void refundPayment(@NonNull String gatewaySessionId, @NonNull BigDecimal amount) {
        try {
            CapturesRefundRequest request = new CapturesRefundRequest(gatewaySessionId);

            RefundRequest refundRequest =new RefundRequest();
            com.paypal.payments.Money money =new com.paypal.payments.Money();
            money.currencyCode("EUR");
            money.value(amount.toPlainString());
            refundRequest.amount(money);
            request.requestBody(refundRequest);

            payPalClient.execute(request);

            log.info("PayPal refund ${} for capture: {}", amount, gatewaySessionId);

        } catch (Exception e) {
            log.error("PayPal refund failed: {}", e.getMessage());
            throw new PaymentException("Failed to process refund: " + e.getMessage());
        }
    }
}