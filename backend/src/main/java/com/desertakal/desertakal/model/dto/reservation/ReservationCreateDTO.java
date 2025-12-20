package com.desertakal.desertakal.model.dto.reservation;

import com.desertakal.desertakal.model.entity.Guide;
import com.desertakal.desertakal.model.entity.Tour;
import com.desertakal.desertakal.model.entity.Tourist;
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
    private LocalDateTime startDate;
    private Integer numberPeople;
    private BigDecimal amount;
    private UUID tourUuid;
    private UUID guideUuid;
}
