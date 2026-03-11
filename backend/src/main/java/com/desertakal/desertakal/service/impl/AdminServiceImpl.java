package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.model.dto.admin.AdminDashboardDTO;
import com.desertakal.desertakal.model.dto.admin.MonthlyStatDTO;
import com.desertakal.desertakal.model.entity.Reservation;
import com.desertakal.desertakal.model.enums.ReservationStatus;
import com.desertakal.desertakal.model.enums.UserStatus;
import com.desertakal.desertakal.repository.*;
import com.desertakal.desertakal.service.interfaces.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final TourRepository tourRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final ArticleRepository articleRepository;

    @Override
    public AdminDashboardDTO getGlobalDashboardStats() {
        log.info("Calcul des statistiques via les entités Java");

        // 2. توزيع الحجوزات حسب الحالة (Java Stream)
        Map<String, Long> reservationsByStatus = reservationRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        res -> res.getStatus().name(),
                        Collectors.counting()
                ));

        List<Reservation> confirmedReservations = reservationRepository.findByStatus(ReservationStatus.CONFIRMED);

        Map<String, List<Reservation>> groupedByMonth = confirmedReservations.stream()
                .collect(Collectors.groupingBy(res ->
                    res.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM"))
                ));

        List<MonthlyStatDTO> monthlyPerformance = groupedByMonth.entrySet().stream()
                .map(entry -> {
                    String month = entry.getKey();
                    List<Reservation> resInMonth = entry.getValue();

                    double revenue = resInMonth.stream()
                            .mapToDouble(r -> r.getAmount().doubleValue())
                            .sum();

                    return MonthlyStatDTO.builder()
                            .month(month)
                            .revenue(revenue)
                            .reservationCount((long) resInMonth.size())
                            .build();
                })
                .sorted(Comparator.comparing(MonthlyStatDTO::getMonth).reversed())
                .toList();
        return AdminDashboardDTO.builder()
                .totalUsers(userRepository.count())
                .totalTours(tourRepository.count())
                .totalReservations(reservationRepository.count())
                .monthlyPerformance(monthlyPerformance)
                .averageTourRating(tourRepository.getAverageRating() != null ? tourRepository.getAverageRating() : 0.0)
                .totalArticles(articleRepository.count())
                .activeGuides(userRepository.countByRole_NameAndStatus("GUIDE", UserStatus.ACTIVE))
                .reservationsByStatus(reservationsByStatus)
                .monthlyPerformance(monthlyPerformance)
                .build();
    }
}
