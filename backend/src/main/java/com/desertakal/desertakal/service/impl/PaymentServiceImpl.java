package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.exception.custom.BusinessRuleException;
import com.desertakal.desertakal.exception.custom.PaymentException;
import com.desertakal.desertakal.exception.custom.ResourceNotFoundException;
import com.desertakal.desertakal.exception.custom.UnauthorizedActionException;
import com.desertakal.desertakal.model.dto.payment.PaymentCreateDTO;
import com.desertakal.desertakal.model.dto.payment.PaymentFindDTO;
import com.desertakal.desertakal.model.dto.payment.PaymentResponseDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.entity.*;
import com.desertakal.desertakal.model.enums.PaymentMethod;
import com.desertakal.desertakal.model.enums.PaymentStatus;
import com.desertakal.desertakal.model.enums.PaymentType;
import com.desertakal.desertakal.model.enums.ReservationStatus;
import com.desertakal.desertakal.model.mapper.PaymentMapper;
import com.desertakal.desertakal.repository.PaymentRepository;
import com.desertakal.desertakal.repository.ReservationRepository;
import com.desertakal.desertakal.service.interfaces.DocumentGeneratorService;
import com.desertakal.desertakal.service.interfaces.NotificationService;
import com.desertakal.desertakal.service.interfaces.PaymentGateway;
import com.desertakal.desertakal.service.interfaces.PaymentService;
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
        log.info("Initiating {} payment for reservation: {}",
                dto.getMethod(), dto.getReservationUuid()
        );

        Reservation reservation =findReservation(dto.getReservationUuid());
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

        PaymentGateway gateway =gatewayFactory.getGateway(dto.getMethod());

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
        log.info("Starting capture process for gatewayPaymentId: {} by tourist: {}", gatewayPaymentId, touristUuid);

        Payment payment = repository.findByGatewayPaymentId(gatewayPaymentId)
                .orElseThrow(() -> {
                    log.error("Capture failed: No payment found for gateway ID: {}", gatewayPaymentId);
                    return new ResourceNotFoundException("Payment", "gatewayPaymentId", gatewayPaymentId);
                });

        validateOwnership(payment.getReservation(), touristUuid);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.warn("Capture aborted: Payment {} is already in status {}", gatewayPaymentId, payment.getStatus());
            throw new BusinessRuleException("Payment already processed. Status: " + payment.getStatus());
        }

        try {
            PaymentGateway gateway = gatewayFactory.getGateway(payment.getMethod());

            log.debug("Calling {} gateway to capture order: {}", payment.getMethod(), gatewayPaymentId);
            String sessionId = gateway.captureOrder(gatewayPaymentId);

            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setGatewaySessionId(sessionId);
            repository.save(payment);

            log.info("Payment captured successfully! Gateway ID: {}, Session ID: {}, Amount: {}",
                    gatewayPaymentId, sessionId, payment.getAmount());

            handlePaymentCompletion(payment);

            return mapper.toFindDto(payment);

        } catch (PaymentException e) {
            log.error("Gateway capture failed for ID: {}. Error: {}", gatewayPaymentId, e.getMessage());
            payment.setStatus(PaymentStatus.FAILED);
            repository.save(payment);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during capture of {}: ", gatewayPaymentId, e);
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

        log.info("Payment {} cancelled by tourist {}",
                paymentUuid, touristUuid);

        return mapper.toFindDto(payment);
    }

    @Override
    public PaymentFindDTO refundPayment(@NonNull UUID paymentUuid, @NonNull UUID adminUuid) {
        return null;
    }

    @Override
    public PaymentFindDTO partialRefundPayment(@NonNull UUID paymentUuid, @NonNull BigDecimal amount, @NonNull UUID adminUuid) {
        return null;
    }

    @Override
    public PaymentFindDTO getPayment(@NonNull UUID paymentUuid) {
        log.info("Fetching payment details for UUID: {}", paymentUuid);
        return mapper.toFindDto(findPayment(paymentUuid));
    }

    @Override
    public PaginationDTO getPaymentsByReservation(@NonNull UUID reservationUuid, @NonNull Pageable pageable) {
        log.info("Fetching payments for reservation: {} [Page: {}, Size: {}]",
                reservationUuid, pageable.getPageNumber(), pageable.getPageSize());

        Specification<@NonNull Payment> spec = getSpecification(reservationUuid, null, null, null, null);
        Page<@NonNull Payment> paymentPage = repository.findAll(spec, pageable);

        log.debug("Found {} payments for reservation: {}", paymentPage.getTotalElements(), reservationUuid);

        return buildPaginationDTO(paymentPage);
    }

    @Override
    public PaginationDTO getPaymentsByTourist(@NonNull UUID touristUuid, PaymentStatus status, @NonNull Pageable pageable) {
        log.info("Fetching payments for tourist: {} with status: {} [Page: {}]",
                touristUuid, status, pageable.getPageNumber());

        Specification<@NonNull Payment> spec = getSpecification(null, touristUuid, status, null, null);
        Page<@NonNull Payment> paymentPage = repository.findAll(spec, pageable);

        return buildPaginationDTO(paymentPage);
    }

    @Override
    public PaginationDTO getAllPayments(PaymentStatus status, PaymentType type, PaymentMethod method, @NonNull Pageable pageable) {
        log.info("Admin search for all payments with filters - Status: {}, Type: {}, Method: {} [Page: {}]",
                status, type, method, pageable.getPageNumber());

        Specification<@NonNull Payment> spec = getSpecification(null, null, status, type, method);
        Page<@NonNull Payment> paymentPage = repository.findAll(spec, pageable);

        log.info("Total payments matching criteria: {}", paymentPage.getTotalElements());

        return buildPaginationDTO(paymentPage);
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

    private void handlePaymentCompletion(Payment payment) {
        Reservation reservation =payment.getReservation();
        BigDecimal netPaid = calculateNetPaid(reservation);

        if (netPaid.compareTo(reservation.getAmount()) >= 0) {
            reservation.setStatus(
                    ReservationStatus.CONFIRMED);

            documentGeneratorService.generateConfirmationAssets(reservation);

            reservationRepository.save(reservation);

            notificationService.create("Payment Confirmed",
                    String.format("Your booking for '%s' is confirmed! Download your ticket.",
                            reservation.getTour().getTitle()
                    ),
                    reservation.getTourist().getUuid()
            );

            notificationService.create("Booking Confirmed",
                    String.format("Booking '%s' by %s confirmed.",
                            reservation.getTour().getTitle(),
                            reservation.getTourist().getFullName()
                    ),
                    reservation.getGuide().getUuid()
            );

            log.info("Reservation {} confirmed. PDF & QR generated.", reservation.getUuid());
        }
    }

    private Reservation findReservation(UUID uuid) {
        return reservationRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("Reservation not found with UUID: {}", uuid);
                    return new ResourceNotFoundException("Reservation", "uuid", uuid.toString());
                });
    }

    private Payment findPayment(UUID uuid) {
        return repository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("Payment not found with UUID: {}", uuid);
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
        if (repository.existsByReservationAndStatus(reservation,PaymentStatus.PENDING)) {
            log.warn("Attempt to initiate payment for reservation {} failed: Pending payment already exists.",
                    reservation.getUuid());
            throw new BusinessRuleException("You already have a pending payment for this reservation. Please complete or cancel it first.");
        }
    }

    private void validateRefundable(Payment payment) {
        if (payment.getStatus() != PaymentStatus.COMPLETED
                && payment.getStatus() != PaymentStatus.REFUNDED_PARTIAL) {
            throw new BusinessRuleException("Only COMPLETED or PARTIALLY_REFUNDED payments can be refunded.");
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

    private void updateReservationAfterRefund(Reservation reservation) {
        BigDecimal netPaid = calculateNetPaid(reservation);

        if (netPaid.compareTo(BigDecimal.ZERO) <= 0) {
            reservation.setStatus(ReservationStatus.CANCELLED);
            documentGeneratorService.generateConfirmationAssets(reservation);
            reservationRepository.save(reservation);
            log.info("Reservation {} cancelled (fully refunded)", reservation.getUuid());
        }
    }

    private void sendRefundNotification(Payment payment, BigDecimal amount, boolean isPartial) {
        String type = isPartial ? "Partial Refund" : "Full Refund";
        String msg = String.format("%s of $%s for '%s'.",
                type, amount.toPlainString(), payment.getReservation().getTour().getTitle()
        );

        notificationService.create("Payment Refunded", msg, payment.getReservation().getTourist().getUuid());
    }

    private Specification<@NonNull Payment> getSpecification(
            UUID reservationUuid,
            UUID touristUuid,
            PaymentStatus status,
            PaymentType type,
            PaymentMethod method) {
        return (root, query, cb) -> {
            query.distinct(true);

            log.debug("Building dynamic Specification for Payment search with parameters: [touristUuid: {}, status: {}, type: {}, method: {}]",
                    touristUuid, status, type, method);

            List<Predicate> predicates = new ArrayList<>();

            Join<Payment, Reservation> reservationJoin = root.join("reservation", JoinType.INNER);
            Join<Reservation, Tourist> touristJoin = reservationJoin.join("tourist", JoinType.INNER);

            if (reservationUuid != null) {
                predicates.add(cb.equal(reservationJoin.get("uuid"), reservationUuid));
                log.debug("Filter applied: reservation.uuid = '{}'", reservationUuid);
            }

            if (touristUuid != null) {
                predicates.add(cb.equal(touristJoin.get("uuid"), touristUuid));
                log.debug("Filter applied: reservation.tourist.uuid = '{}'", touristUuid);
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
                log.debug("Filter applied: status = '{}'", status);
            }

            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
                log.debug("Filter applied: type = '{}'", type);
            }

            if (method != null) {
                predicates.add(cb.equal(root.get("method"), method));
                log.debug("Filter applied: method = '{}'", method);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
