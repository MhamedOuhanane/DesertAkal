package com.desertakal.desertakal.model.enums;

import lombok.Getter;

@Getter
public enum ReservationStatus {
    PENDING("Reservation is pending"),
    CONFIRMED("Reservation is confirmed"),
    CANCELLED("Reservation has been cancelled"),
    REJECTED("Reservation has been rejected"),
    COMPLETED("Reservation has been completed");

    private final String desc;

    ReservationStatus(String desc) {this.desc = desc;}
}
