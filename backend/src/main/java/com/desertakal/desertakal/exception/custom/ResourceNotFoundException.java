package com.desertakal.desertakal.exception.custom;

import com.desertakal.desertakal.exception.base.BusinessException;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resource,String field,String value) {
        super(
                String.format("%s not found with %s: [%s]", resource, field, value),
                HttpStatus.NOT_FOUND
        );
    }
}
