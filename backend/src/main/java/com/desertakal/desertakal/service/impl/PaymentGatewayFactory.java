package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.exception.custom.PaymentException;
import com.desertakal.desertakal.model.enums.PaymentMethod;
import com.desertakal.desertakal.service.interfaces.PaymentGateway;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PaymentGatewayFactory {
    private final Map<PaymentMethod, PaymentGateway> gateways;

    public PaymentGatewayFactory(List<PaymentGateway> gatewayList) {
        this.gateways = gatewayList.stream()
                .collect(Collectors.toMap(
                        PaymentGateway::getMethod,
                        Function.identity()
                ));

        log.info("Payment gateways registered: {}", gateways.keySet());
    }

    public PaymentGateway getGateway(@NonNull PaymentMethod method) {
        PaymentGateway gateway = gateways.get(method);

        if (gateway == null) {
            throw new PaymentException(
                    "Unsupported payment method: "
                            + method
                            + ". Available: "
                            + gateways.keySet());
        }

        return gateway;
    }
}
