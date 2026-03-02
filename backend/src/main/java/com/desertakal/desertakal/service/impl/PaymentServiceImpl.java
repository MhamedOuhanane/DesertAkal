package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.exception.custom.*;
import com.desertakal.desertakal.model.dto.payment.*;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.entity.*;
import com.desertakal.desertakal.model.enums.*;
import com.desertakal.desertakal.model.mapper.PaymentMapper;
import com.desertakal.desertakal.repository.PaymentRepository;
import com.desertakal.desertakal.repository.ReservationRepository;
import com.desertakal.desertakal.service.interfaces.*;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository repository;
    private final PaymentMapper mapper;
    private final NotificationService notificationService;
    private final PaymentGatewayFactory gatewayFactory;
    private final ReservationRepository reservationRepository;
    private final DocumentGeneratorService documentGeneratorService;

    @Override
    @Transactional
    public PaymentResponseDTO initiatePayment(@NonNull PaymentCreateDTO dto, @NonNull UUID touristUuid) {

        log.info("Initiating {} payment for reservation: {}", dto.getMethod(), dto.getReservationUuid());

        Reservation reservation = findReservation(dto.getReservationUuid());
        validateOwnership(reservation, touristUuid);
        validatePayableStatus(reservation);
        rejectIfPendingPaymentExists(reservation);

        BigDecimal remaining = calculateRemainingAmount(reservation);

        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("This reservation is already fully paid.");
        }

        Payment payment = Payment.builder()
                .amount(remaining)
                .type(PaymentType.PAYMENT)
                .status(PaymentStatus.PENDING)
                .method(dto.getMethod())
                .reservation(reservation)
                .build();
        payment = repository.save(payment);

        PaymentGateway gateway = gatewayFactory.getGateway(dto.getMethod());
        String approvalUrl = gateway.createOrder(payment, reservation);

        log.info("Payment {} initiated via {}. Amount: ${}",
                payment.getUuid(), dto.getMethod(), remaining);

        return PaymentResponseDTO.builder()
                .paymentUuid(payment.getUuid().toString())
                .approvalUrl(approvalUrl)
                .gatewayPaymentId(payment.getGatewayPaymentId())
                .method(dto.getMethod().name())
                .status(PaymentStatus.PENDING.name())
                .build();
    }

    @Override
    @Transactional
    public PaymentFindDTO capturePayment(@NonNull String gatewayPaymentId, @NonNull UUID touristUuid) {

        log.info("Starting capture for gatewayPaymentId: {} by tourist: {}", gatewayPaymentId, touristUuid);

        Payment payment = repository.findByGatewayPaymentId(gatewayPaymentId)
                .orElseThrow(() -> {
                    log.error("No payment found for gateway ID: {}", gatewayPaymentId);
                    return new ResourceNotFoundException("Payment", "gatewayPaymentId", gatewayPaymentId);
                });

        validateOwnership(payment.getReservation(), touristUuid);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.warn("Payment {} already in status {}", gatewayPaymentId, payment.getStatus());
            throw new BusinessRuleException("Payment already processed. Status: " + payment.getStatus());
        }

        try {
            PaymentGateway gateway = gatewayFactory.getGateway(payment.getMethod());

            log.debug("Calling {} gateway to capture: {}", payment.getMethod(), gatewayPaymentId);
            String sessionId = gateway.captureOrder(gatewayPaymentId);

            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setGatewaySessionId(sessionId);
            repository.save(payment);

            log.info("Payment captured! Gateway: {}, Session: {}, Amount: {}",
                    gatewayPaymentId, sessionId, payment.getAmount());

            handlePaymentCompletion(payment);

            return mapper.toFindDto(payment);

        } catch (PaymentException e) {
            log.error("Gateway capture failed for {}: {}", gatewayPaymentId, e.getMessage());
            payment.setStatus(PaymentStatus.FAILED);
            repository.save(payment);
            throw e;

        } catch (Exception e) {
            log.error("Unexpected error during capture of {}: ", gatewayPaymentId, e);
            payment.setStatus(PaymentStatus.FAILED);
            repository.save(payment);
            throw new PaymentException("Internal error during payment capture");
        }
    }

    @Override
    @Transactional
    public PaymentFindDTO cancelPayment(@NonNull UUID paymentUuid, @NonNull UUID touristUuid) {

        log.info("Cancelling payment: {}", paymentUuid);

        Payment payment = findPayment(paymentUuid);
        validateOwnership(payment.getReservation(), touristUuid);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessRuleException("Only pending payments can be cancelled. Current status: " + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.CANCELED);
        repository.save(payment);

        log.info("Payment {} cancelled by tourist {}", paymentUuid, touristUuid);
        return mapper.toFindDto(payment);
    }


    @Override
    @Transactional
    public PaymentFindDTO refundPayment(@NonNull UUID paymentUuid, @NonNull UUID adminUuid) {

        log.info("Admin {} full refund for payment {}", adminUuid, paymentUuid);

        Payment original = findPayment(paymentUuid);
        validateRefundable(original);

        BigDecimal alreadyRefunded = repository.getTotalRefundedForSession(original.getGatewaySessionId());
        BigDecimal refundable = original.getAmount().subtract(alreadyRefunded);

        if (refundable.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Payment already fully refunded. Nothing left to refund.");
        }

        PaymentGateway gateway = gatewayFactory.getGateway(original.getMethod());
        gateway.refundPayment(original.getGatewaySessionId(), refundable);

        Payment refund = createRefundPayment(original, refundable);

        original.setStatus(PaymentStatus.REFUNDED);
        repository.save(original);

        updateReservationAfterRefund(original.getReservation());
        sendRefundNotification(original, refundable, false);

        log.info("Full refund ${} processed for payment {}", refundable, paymentUuid);
        return mapper.toFindDto(refund);
    }

    @Override
    @Transactional
    public PaymentFindDTO partialRefundPayment(@NonNull UUID paymentUuid, @NonNull BigDecimal amount, @NonNull UUID adminUuid) {

        log.info("Admin {} partial refund ${} for payment {}",
                adminUuid, amount, paymentUuid);

        Payment original = findPayment(paymentUuid);
        validateRefundable(original);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Refund amount must be greater than zero.");
        }

        BigDecimal alreadyRefunded = repository.getTotalRefundedForSession(original.getGatewaySessionId());
        BigDecimal refundable = original.getAmount().subtract(alreadyRefunded);

        if (amount.compareTo(refundable) > 0) {
            throw new BusinessRuleException(String.format("Requested amount ($%s) exceeds refundable balance ($%s).",
                    amount.toPlainString(), refundable.toPlainString()));
        }

        PaymentGateway gateway = gatewayFactory.getGateway(original.getMethod());
        gateway.refundPayment(original.getGatewaySessionId(), amount);

        Payment refund = createRefundPayment(original, amount);

        BigDecimal totalRefunded = alreadyRefunded.add(amount);
        original.setStatus(
                totalRefunded.compareTo(original.getAmount()) >= 0
                        ? PaymentStatus.REFUNDED
                        : PaymentStatus.REFUNDED_PARTIAL);
        repository.save(original);

        updateReservationAfterRefund(original.getReservation());
        sendRefundNotification(original, amount, true);

        log.info("Partial refund ${} for payment {}. Total: ${}/{}",
                amount, paymentUuid, totalRefunded, original.getAmount());
        return mapper.toFindDto(refund);
    }

    @Override
    @Transactional
    public void processRefundOnCancel(@NonNull Reservation reservation, boolean isAdmin) {

        log.info("Processing cancel refund for reservation: {} (by: {})",
                reservation.getUuid(), isAdmin ? "ADMIN" : "TOURIST");

        List<@NonNull Payment> completedPayments = repository.findCompletedPaymentsByReservation(reservation);

        if (completedPayments.isEmpty()) {
            log.info("No completed payments for reservation: {}", reservation.getUuid());
            return;
        }

        BigDecimal refundFactor = isAdmin
                ? BigDecimal.ONE
                : BigDecimal.valueOf(0.90);

        BigDecimal totalRefundAmount = reservation.getAmount()
                .multiply(refundFactor)
                .setScale(2, RoundingMode.HALF_UP);

        log.info("Reservation {}: Total: ${}, Refundable: ${} ({}%)",
                reservation.getUuid(),
                reservation.getAmount(),
                totalRefundAmount,
                refundFactor.multiply(BigDecimal.valueOf(100)).intValue()
        );

        BigDecimal remaining = totalRefundAmount;

        for (Payment original : completedPayments) {

            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal captureMax = original.getAmount();
            BigDecimal refundForThis = remaining.min(captureMax);

            try {
                PaymentGateway gateway = gatewayFactory.getGateway(original.getMethod());
                gateway.refundPayment(original.getGatewaySessionId(), refundForThis);
            } catch (Exception e) {
                log.error("Gateway refund failed for payment {}: {}",
                        original.getUuid(), e.getMessage());
                throw new PaymentException("Refund failed. Cancellation aborted.");
            }

            createRefundPayment(original, refundForThis);

            boolean fullyRefunded = refundForThis.compareTo(captureMax) >= 0;
            original.setStatus(fullyRefunded
                    ? PaymentStatus.REFUNDED
                    : PaymentStatus.REFUNDED_PARTIAL);
            repository.save(original);

            remaining = remaining.subtract(refundForThis);

            log.info("Refunded ${} from payment {} (remaining: ${})",
                    refundForThis, original.getUuid(), remaining);
        }

        sendCancelRefundNotification(reservation, totalRefundAmount, isAdmin);
    }

    @Override
    public PaymentFindDTO getPayment(@NonNull UUID paymentUuid) {
        log.info("Fetching payment: {}", paymentUuid);
        return mapper.toFindDto(findPayment(paymentUuid));
    }

    @Override
    public PaginationDTO getPaymentsByReservation(
            @NonNull UUID reservationUuid,
            @NonNull Pageable pageable) {

        log.info("Fetching payments for reservation: {} [Page: {}, Size: {}]",
                reservationUuid, pageable.getPageNumber(), pageable.getPageSize());

        Specification<@NonNull Payment> spec = buildSpecification(reservationUuid, null, null, null, null);
        Page<@NonNull Payment> page = repository.findAll(spec, pageable);

        log.debug("Found {} payments for reservation: {}",
                page.getTotalElements(), reservationUuid);
        return buildPaginationDTO(page);
    }

    @Override
    public PaginationDTO getPaymentsByTourist(@NonNull UUID touristUuid, PaymentStatus status, @NonNull Pageable pageable) {

        log.info("Fetching payments for tourist: {} status: {} [Page: {}]",
                touristUuid, status, pageable.getPageNumber());

        Specification<@NonNull Payment> spec = buildSpecification(null, touristUuid, status, null, null);
        Page<@NonNull Payment> page = repository.findAll(spec, pageable);

        return buildPaginationDTO(page);
    }

    @Override
    public PaginationDTO getAllPayments(PaymentStatus status, PaymentType type, PaymentMethod method, @NonNull Pageable pageable) {

        log.info("Admin payments search - Status: {}, Type: {}, Method: {} [Page: {}]",
                status, type, method, pageable.getPageNumber());

        Specification<@NonNull Payment> spec = buildSpecification(null, null, status, type, method);
        Page<@NonNull Payment> page = repository.findAll(spec, pageable);

        log.info("Total payments found: {}", page.getTotalElements());
        return buildPaginationDTO(page);
    }

    private void handlePaymentCompletion(Payment payment) {
        Reservation reservation = payment.getReservation();
        BigDecimal netPaid = calculateNetPaid(reservation);

        if (netPaid.compareTo(reservation.getAmount()) >= 0) {
            reservation.setStatus(ReservationStatus.CONFIRMED);
            documentGeneratorService.generateConfirmationAssets(reservation);
            reservationRepository.save(reservation);

            notificationService.create("Payment Confirmed",
                    String.format(
                            "Your booking for '%s' is confirmed! Download your ticket.",
                            reservation.getTour().getTitle()
                    ),
                    reservation.getTourist().getUuid()
            );

            notificationService.create("Booking Confirmed",
                    String.format(
                            "Booking '%s' by %s confirmed.",
                            reservation.getTour().getTitle(),
                            reservation.getTourist().getFullName()
                    ),
                    reservation.getGuide().getUuid()
            );

            log.info("Reservation {} confirmed. PDF & QR generated.",
                    reservation.getUuid());
        }
    }

    private Payment createRefundPayment(Payment original, BigDecimal amount) {
        Payment refund = Payment.builder()
                .amount(amount)
                .type(PaymentType.REFUND)
                .status(PaymentStatus.COMPLETED)
                .method(original.getMethod())
                .reservation(original.getReservation())
                .gatewaySessionId(original.getGatewaySessionId())
                .build();
        return repository.save(refund);
    }

    private void updateReservationAfterRefund(Reservation reservation) {
        BigDecimal netPaid = calculateNetPaid(reservation);

        if (netPaid.compareTo(BigDecimal.ZERO) <= 0) {
            reservation.setStatus(ReservationStatus.CANCELLED);
            documentGeneratorService.generateConfirmationAssets(reservation);
            reservationRepository.save(reservation);
            log.info("Reservation {} cancelled (fully refunded)",
                    reservation.getUuid());
        }
    }

    private void sendRefundNotification(
            Payment payment, BigDecimal amount, boolean isPartial) {

        String type = isPartial ? "Partial Refund" : "Full Refund";
        String msg = String.format("%s of $%s for '%s'.",
                type, amount.toPlainString(),
                payment.getReservation().getTour().getTitle()
        );

        notificationService.create("Payment Refunded", msg, payment.getReservation().getTourist().getUuid());
    }

    private void sendCancelRefundNotification(Reservation reservation, BigDecimal refundAmount, boolean isAdmin) {
        String tourTitle = reservation.getTour().getTitle();
        String touristName = reservation.getTourist().getFullName();
        String messageToTourist;
        String messageToGuide;

        if (isAdmin) {
            messageToTourist = String.format(
                    "Your reservation for '%s' was cancelled by administration. Full refund of $%s has been processed.",
                    tourTitle, refundAmount.toPlainString());

            messageToGuide = String.format(
                    "Reservation for '%s' by %s was cancelled by administration. The date is now available.",
                    tourTitle, touristName);
        } else {
            BigDecimal fee = reservation.getAmount().subtract(refundAmount);
            messageToTourist = String.format(
                    "You cancelled your reservation for '%s'. Refund: $%s (90%%). Cancellation fee: $%s (10%%).",
                    tourTitle, refundAmount.toPlainString(), fee.toPlainString());

            messageToGuide = String.format(
                    "Tourist %s has cancelled their reservation for '%s'. The slot is now open.",
                    touristName, tourTitle);
        }

        notificationService.create("Cancellation Refund", messageToTourist, reservation.getTourist().getUuid());
        notificationService.create("Booking Cancelled", messageToGuide, reservation.getGuide().getUuid());

        log.info("Cancellation notifications sent to tourist {} and guide {}",
                reservation.getTourist().getUuid(), reservation.getGuide().getUuid());
    }

    private Reservation findReservation(UUID uuid) {
        return reservationRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("Reservation not found: {}", uuid);
                    return new ResourceNotFoundException("Reservation", "uuid", uuid.toString());
                });
    }

    private Payment findPayment(UUID uuid) {
        return repository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("Payment not found: {}", uuid);
                    return new ResourceNotFoundException("Payment", "uuid", uuid.toString());
                });
    }

    private void validateOwnership(Reservation reservation, UUID touristUuid) {
        if (!reservation.getTourist().getUuid().equals(touristUuid)) {
            throw new UnauthorizedActionException("Not the owner of this reservation.");
        }
    }

    private void validatePayableStatus(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessRuleException("Cannot pay for reservation with status: " + reservation.getStatus());
        }
    }

    private void rejectIfPendingPaymentExists(Reservation reservation) {
        if (repository.existsByReservationAndStatus(reservation, PaymentStatus.PENDING)) {
            log.warn("Pending payment exists for reservation {}", reservation.getUuid());
            throw new BusinessRuleException("You already have a pending payment for this reservation. Please complete or cancel it first.");
        }
    }

    private void validateRefundable(Payment payment) {
        if (payment.getStatus() != PaymentStatus.COMPLETED
                && payment.getStatus() != PaymentStatus.REFUNDED_PARTIAL) {
            throw new BusinessRuleException("Only COMPLETED or PARTIALLY_REFUNDED payments can be refunded. Current: " + payment.getStatus());
        }
        if (payment.getType() == PaymentType.REFUND) {
            throw new BusinessRuleException("Cannot refund a refund.");
        }
        if (payment.getGatewaySessionId() == null) {
            throw new PaymentException("No gateway session found for this payment.");
        }
    }

    private BigDecimal calculateRemainingAmount(Reservation reservation) {
        return reservation.getAmount().subtract(calculateNetPaid(reservation));
    }

    private BigDecimal calculateNetPaid(Reservation reservation) {
        BigDecimal paid = repository.getTotalPaidForReservation(reservation, PaymentStatus.COMPLETED);
        BigDecimal refunded = repository.getTotalRefundedForReservation(reservation);
        return paid.subtract(refunded);
    }

    private Specification<@NonNull Payment> buildSpecification(UUID reservationUuid,    UUID touristUuid,   PaymentStatus status,   PaymentType type,   PaymentMethod method) {

        return (root, query, cb) -> {
            query.distinct(true);

            List<Predicate> predicates = new ArrayList<>();

            if (reservationUuid != null || touristUuid != null) {
                Join<Payment, Reservation> reservationJoin = root.join("reservation", JoinType.INNER);

                if (reservationUuid != null) {
                    predicates.add(cb.equal(reservationJoin.get("uuid"), reservationUuid));
                }

                if (touristUuid != null) {
                    Join<Reservation, Tourist> touristJoin = reservationJoin.join("tourist", JoinType.INNER);
                    predicates.add(cb.equal(touristJoin.get("uuid"), touristUuid));
                }
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }

            if (method != null) {
                predicates.add(cb.equal(root.get("method"), method));
            }

            log.debug("Payment spec filters - reservation: {}, tourist: {}, status: {}, type: {}, method: {}",
                    reservationUuid, touristUuid, status, type, method);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private PaginationDTO buildPaginationDTO(Page<@NonNull Payment> page) {
        return PaginationDTO.builder()
                .content(mapper.toDtos(page.getContent()))
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }
}