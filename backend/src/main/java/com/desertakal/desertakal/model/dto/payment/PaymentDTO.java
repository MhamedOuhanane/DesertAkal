package com.desertakal.desertakal.model.dto.payment;

import com.desertakal.desertakal.model.enums.PaymentStatus;
import com.desertakal.desertakal.model.enums.PaymentType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {

    private UUID uuid;
    private LocalDateTime date;
    private BigDecimal amount;
    private PaymentStatus status;
    private PaymentType type;
    private String method;

    private UUID reservationUuid;
    private String touristName;
    private String touristPhoto;
}
