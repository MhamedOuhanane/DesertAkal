package com.desertakal.desertakal.model.dto.reservation;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationUpdateDTO {
    private LocalDateTime startDate;
    private Integer numberPeople;
    private BigDecimal amount;
    private UUID guideUuid;
}
