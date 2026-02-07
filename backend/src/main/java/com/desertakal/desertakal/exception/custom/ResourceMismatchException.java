package com.desertakal.desertakal.exception.custom;

import com.desertakal.desertakal.exception.base.BusinessException;
import org.springframework.http.HttpStatus;

public class ResourceMismatchException extends BusinessException {
    public ResourceMismatchException(String resource, String parent, String value) {
        super(
                String.format("%s does not belong to %s: [%s]", resource, parent, value),
                HttpStatus.BAD_REQUEST
        );
    }
}