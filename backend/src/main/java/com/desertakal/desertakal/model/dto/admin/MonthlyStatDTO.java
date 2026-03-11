package com.desertakal.desertakal.model.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MonthlyStatDTO {
    private String month;
    private double revenue;
    private long reservationCount;
}
