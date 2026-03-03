package com.desertakal.desertakal.model.dto.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequestDTO {

    @NotNull(message = "Refund amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least $0.01")
    private BigDecimal amount;
}
