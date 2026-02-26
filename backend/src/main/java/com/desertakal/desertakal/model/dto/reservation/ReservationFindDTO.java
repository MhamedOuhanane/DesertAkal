package com.desertakal.desertakal.model.dto.reservation;

import com.desertakal.desertakal.model.dto.payment.PaymentDTO;
import com.desertakal.desertakal.model.enums.ReservationStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationFindDTO {
    private UUID uuid;

    private LocalDateTime date;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Integer numberPeople;
    private BigDecimal amount;
    private ReservationStatus status;

    private String qrCode;
    private String pdfUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UUID tourUuid;
    private String tourTitle;

    private UUID guideUuid;
    private String guideName;
    private String guidePhoto;

    private UUID touristUuid;
    private String touristName;
    private String touristPhoto;

    private List<PaymentDTO> payments;
}
