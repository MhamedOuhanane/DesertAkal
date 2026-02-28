package com.desertakal.desertakal.model.dto.payment;

import com.desertakal.desertakal.model.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreateDTO {

    @NotNull(message = "Reservation UUID is required")
    private UUID reservationUuid;

    @NotNull(message = "Payment method is required")
    private PaymentMethod method;
}
