package com.desertakal.desertakal.model.enums;

import lombok.Getter;

@Getter
public enum PaymentType {
    PAYMENT("Make a payment"),
    REFUND("Refunded amount");

    private final String desc;

    PaymentType(String desc) {this.desc = desc;}
}
