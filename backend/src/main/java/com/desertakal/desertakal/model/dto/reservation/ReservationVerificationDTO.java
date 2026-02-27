package com.desertakal.desertakal.model.dto.reservation;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class ReservationVerificationDTO {
    private UUID uuid;
    private String touristName;
    private String tourTitle;
    private LocalDate startDate;
    private Integer numberPeople;
    private String status;
    private String reference;
    private boolean isValid;
}
