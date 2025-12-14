package com.desertakal.desertakal.model.dto.reservation;

import com.desertakal.desertakal.model.enums.ReservationStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDTO {
    private UUID uuid;
    private LocalDateTime date;
    private LocalDateTime startDate;
    private Integer numberPeople;
    private BigDecimal amount;
    private ReservationStatus status;
    private UUID tourUuid;
    private String tourName;
    private UUID guideUuid;
    private String guideName;
    private UUID touristUuid;
    private String touristName;
}
