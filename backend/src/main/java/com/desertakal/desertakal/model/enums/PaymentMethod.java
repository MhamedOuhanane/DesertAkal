package com.desertakal.desertakal.model.enums;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    PAYPAL("PayPal");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }
}