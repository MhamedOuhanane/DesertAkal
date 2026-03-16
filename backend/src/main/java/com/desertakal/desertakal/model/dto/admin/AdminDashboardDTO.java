package com.desertakal.desertakal.model.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class AdminDashboardDTO {
    private long totalUsers;
    private long totalTours;
    private long totalReservations;
    private BigDecimal totalRevenue;
    private double averageTourRating;

    private long totalArticles;
    private long activeGuides;

    private Map<String, Long> reservationsByStatus;
    private List<MonthlyStatDTO> monthlyPerformance;
}