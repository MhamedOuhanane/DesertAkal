package com.desertakal.desertakal.exception.custom;

import com.desertakal.desertakal.exception.base.BusinessException;
import org.springframework.http.HttpStatus;

public class DocumentGenerationException extends BusinessException {
    public DocumentGenerationException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}