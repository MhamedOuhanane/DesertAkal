package com.desertakal.desertakal.repository;

import com.desertakal.desertakal.model.entity.Payment;
import com.desertakal.desertakal.model.entity.Reservation;
import com.desertakal.desertakal.model.enums.PaymentStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<@NonNull Payment, @NonNull UUID>, JpaSpecificationExecutor<@NonNull Payment> {

    Optional<Payment> findByUuid(UUID uuid);

    Optional<Payment> findByGatewayPaymentId(String gatewayPaymentId);

    @EntityGraph(attributePaths = {
            "reservation",
            "reservation.tour",
            "reservation.guide",
            "reservation.tourist"
    })
    boolean existsByReservationAndStatus(Reservation reservation, PaymentStatus status);

    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
            FROM Payment p
                WHERE p.reservation = :reservation
                    AND p.status = :status
                        AND p.type = 'PAYMENT'
    """)
    BigDecimal getTotalPaidForReservation(@Param("reservation") Reservation reservation, @Param("status") PaymentStatus status);

    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
            FROM Payment p
                WHERE p.gatewaySessionId = :sessionId
                    AND p.type = 'REFUND'
                        AND p.status = 'COMPLETED'
    """)
    BigDecimal getTotalRefundedForSession(@Param("sessionId") String sessionId);

    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
            FROM Payment p
                WHERE p.reservation = :reservation
                    AND p.type = 'REFUND'
                        AND p.status = 'COMPLETED'
    """)
    BigDecimal getTotalRefundedForReservation(@Param("reservation") Reservation reservation);

    @Query("""
        SELECT p FROM Payment p
            WHERE p.reservation = :reservation
                AND p.status = 'COMPLETED'
                    AND p.type = 'PAYMENT'
                        ORDER BY p.createdAt ASC
    """)
    List<Payment> findCompletedPaymentsByReservation(@Param("reservation") Reservation reservation);
}