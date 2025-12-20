package com.desertakal.desertakal.exception.custom;

import com.desertakal.desertakal.exception.base.BusinessException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class GuideNotAvailableException extends BusinessException {
    public GuideNotAvailableException(String guideName, LocalDate date) {
        super(String.format("Guide %s is not available on %s",
                guideName, date.toString()), HttpStatus.BAD_REQUEST);
    }
}
