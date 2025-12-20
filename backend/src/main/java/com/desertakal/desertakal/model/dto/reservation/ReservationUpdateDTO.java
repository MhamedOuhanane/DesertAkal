package com.desertakal.desertakal.model.dto.reservation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
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

    @Min(value = 1, message = "Number of people must be at least 1")
    private Integer numberPeople;

    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private UUID guideUuid;
}
