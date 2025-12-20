package com.desertakal.desertakal.exception.custom;

import com.desertakal.desertakal.exception.base.BusinessException;
import com.desertakal.desertakal.model.enums.ReservationStatus;
import org.springframework.http.HttpStatus;

public class ReservationStatusException extends BusinessException {
    public ReservationStatusException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public ReservationStatusException(String action, ReservationStatus currentStatus) {
        super(String.format("Cannot perform action '%s' because reservation is already %s",
                action, currentStatus.name()), HttpStatus.BAD_REQUEST);
    }
}
