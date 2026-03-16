package com.desertakal.desertakal.model.dto.reservation;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class ReservationVerificationDTO {
    private UUID uuid;

    private String tourTitle;

    private String touristName;
    private String touristPhoto;

    private String guideName;
    private String guidePhoto;

    private LocalDate startDate;
    private BigDecimal amount;
    private LocalDate endDate;
    private Integer numberPeople;

    private String status;
    private String reference;
    private boolean isValid;
}
