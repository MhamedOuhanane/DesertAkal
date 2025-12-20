package com.desertakal.desertakal.model.dto.reservation;

import com.desertakal.desertakal.model.entity.Guide;
import com.desertakal.desertakal.model.entity.Tour;
import com.desertakal.desertakal.model.entity.Tourist;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class ReservationCreateDTO {
    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "Number of people is required")
    @Min(value = 1, message = "Number of people must be at least 1")
    private Integer numberPeople;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Tour UUID is required")
    private UUID tourUuid;

    @NotNull(message = "Guide UUID is required")
    private UUID guideUuid;
}
