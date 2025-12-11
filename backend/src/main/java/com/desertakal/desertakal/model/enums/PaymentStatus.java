package com.desertakal.desertakal.model.enums;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    PENDING("Payment is pending"),
    COMPLETED("Payment completed successfully"),
    FAILED("Payment failed"),
    CANCELED("Payment was canceled"),
    REFUNDED("Payment fully refunded"),
    REFUNDED_PARTIAL("Payment partially refunded");

    private final String desc;

    PaymentStatus(String desc) {this.desc = desc;}
}
