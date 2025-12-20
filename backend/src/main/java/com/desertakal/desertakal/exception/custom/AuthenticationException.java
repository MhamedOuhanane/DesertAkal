package com.desertakal.desertakal.exception.custom;

import com.desertakal.desertakal.exception.base.BusinessException;
import org.springframework.http.HttpStatus;

public class AuthenticationException extends BusinessException {
    public AuthenticationException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
