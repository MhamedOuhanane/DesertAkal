package com.desertakal.desertakal.exception.custom;

import com.desertakal.desertakal.exception.base.BusinessException;
import org.springframework.http.HttpStatus;

public class EmailValidationTokenException extends BusinessException {
    public EmailValidationTokenException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
