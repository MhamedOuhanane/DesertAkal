package com.desertakal.desertakal.model.dto.payment;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {
    private String uuid;
    private String approvalUrl;
    private String gatewayPaymentId;
    private String status;
}
