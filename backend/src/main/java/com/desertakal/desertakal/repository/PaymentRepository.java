package com.desertakal.desertakal.repository;

import com.desertakal.desertakal.model.entity.Payment;
import com.desertakal.desertakal.model.entity.Reservation;
import com.desertakal.desertakal.model.enums.PaymentStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<@NonNull Payment, @NonNull UUID>, JpaSpecificationExecutor<@NonNull Payment> {
    Optional<Payment> findByUuid(UUID uuid);

    Optional<Payment> findByGatewaySessionId(String gatewaySessionId);

    boolean existsByReservationAndStatus(
            Reservation reservation, PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
            "WHERE p.reservation = :reservation " +
            "AND p.status = :status " +
            "AND p.type = 'PAYMENT'")
    BigDecimal getTotalPaidForReservation(
            @Param("reservation") Reservation reservation,
            @Param("status") PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
            "WHERE p.gatewayPaymentId = :captureId " +
            "AND p.type = 'REFUND' " +
            "AND p.status = 'COMPLETED'")
    BigDecimal getTotalRefundedForCapture(
            @Param("captureId") String captureId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
            "WHERE p.reservation = :reservation " +
            "AND p.type = 'REFUND' " +
            "AND p.status = 'COMPLETED'")
    BigDecimal getTotalRefundedForReservation(
            @Param("reservation") Reservation reservation);


    @Query("SELECT p FROM Payment p " +
            "WHERE p.reservation = :reservation " +
            "AND p.status = 'COMPLETED' " +
            "AND p.type = 'PAYMENT' " +
            "ORDER BY p.createdAt ASC")
    List<Payment> findCompletedPaymentsByReservation(
            @Param("reservation") Reservation reservation);

//    @Query("SELECT p FROM Payment p " +
//            "WHERE p.reservation.uuid = :uuid " +
//            "ORDER BY p.createdAt DESC")
//    Page<Payment> findByReservationUuid(
//            @Param("uuid") UUID uuid, Pageable pageable);
//
//    @Query("SELECT p FROM Payment p " +
//            "WHERE p.reservation.tourist.uuid = :uuid " +
//            "AND (:status IS NULL OR p.status = :status) " +
//            "ORDER BY p.createdAt DESC")
//    Page<Payment> findByTouristUuid(
//            @Param("uuid") UUID uuid,
//            @Param("status") PaymentStatus status,
//            Pageable pageable);
//
//    Page<Payment> findByStatus(
//            PaymentStatus status, Pageable pageable);
}
