package com.desertakal.desertakal.exception.custom;

import com.desertakal.desertakal.exception.base.BusinessException;
import org.springframework.http.HttpStatus;

public class PaymentException extends BusinessException {
    public PaymentException(String message) {
        super(message, HttpStatus.PAYMENT_REQUIRED);
    }
}
