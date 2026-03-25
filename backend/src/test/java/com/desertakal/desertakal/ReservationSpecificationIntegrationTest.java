package com.desertakal.desertakal;

import com.desertakal.desertakal.model.entity.Guide;
import com.desertakal.desertakal.model.entity.Reservation;
import com.desertakal.desertakal.model.entity.Tour;
import com.desertakal.desertakal.model.entity.Tourist;
import com.desertakal.desertakal.model.enums.ReservationStatus;
import com.desertakal.desertakal.repository.ReservationRepository;
import com.desertakal.desertakal.service.impl.ReservationServiceImpl;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.test.database.replace=none",
        "spring.datasource.url=jdbc:tc:postgresql:16:///testdb"
})
class ReservationSpecificationIntegrationTest {

    @Autowired
    ReservationRepository repository;

    @Autowired
    TestEntityManager entityManager;

    ReservationServiceImpl reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationServiceImpl(
                repository, null, null, null, null,
                null, null, null, null
        );
    }


    @Test
    @DisplayName("Specification: Filter by guide name works correctly.")
    void specification_filterByGuideName() {
        Guide guide1 = Guide.builder().firstName("Ahmed").lastName("Benali").build();
        Guide guide2 = Guide.builder().firstName("Mohamed").lastName("Bouzid").build();

        entityManager.persist(guide1);
        entityManager.persist(guide2);

        entityManager.persist(buildTestReservation(guide1));
        entityManager.persist(buildTestReservation(guide2));

        Page<@NonNull Reservation> result = repository.findAll(
                reservationService.getSpecification(null, null, null, "ahmed", null, null, null, null),
                Pageable.unpaged()
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Specification: Filter by status works correctly.")
    void specification_filterByStatus() {
        entityManager.persist(buildTestReservationWithStatus(ReservationStatus.CONFIRMED));
        entityManager.persist(buildTestReservationWithStatus(ReservationStatus.CANCELLED));

        Page<@NonNull Reservation> result = repository.findAll(
                reservationService.getSpecification(null, null, null, null, null, ReservationStatus.CONFIRMED, null, null),
                Pageable.unpaged()
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Specification: Filter by date works correctly.")
    void specification_filterByDateRange() {

        Reservation reservationNextWeek = buildTestReservationWithStatus(ReservationStatus.CONFIRMED);
        reservationNextWeek.setStartDate(LocalDateTime.now().plusWeeks(1));
        entityManager.persist(reservationNextWeek);

        Reservation reservationNextMonth = buildTestReservationWithStatus(ReservationStatus.CONFIRMED);
        reservationNextMonth.setStartDate(LocalDateTime.now().plusWeeks(4));
        entityManager.persist(reservationNextMonth);

        entityManager.flush();

        LocalDateTime filterDate = LocalDateTime.now().plusWeeks(2);

        Page<@NonNull Reservation> result = repository.findAll(
                reservationService.getSpecification(null, null, null, null, null, null, filterDate, null),
                Pageable.unpaged()
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    private Reservation buildTestReservation(Guide guide) {
        Tour testTour = Tour.builder()
                .title("Test Tour")
                .durationDays(3)
                .description("Test description")
                .image("test-image.png")
                .build();
        entityManager.persist(testTour);

        Tourist testTourist = Tourist.builder()
                .firstName("Test")
                .lastName("Tourist")
                .build();
        entityManager.persist(testTourist);

        return Reservation.builder()
                .tour(testTour)
                .guide(guide)
                .tourist(testTourist)
                .status(ReservationStatus.PENDING)
                .startDate(LocalDateTime.now().plusWeeks(2))
                .endDate(LocalDateTime.now().plusWeeks(2).plusDays(3))
                .numberPeople(2)
                .amount(new BigDecimal("1200.00"))
                .qrCode("test-qr.png")
                .pdfUrl("test.pdf")
                .build();
    }

    private Reservation buildTestReservationWithStatus(ReservationStatus status) {
        Guide tempGuide = Guide.builder()
                .firstName("Test")
                .lastName("Guide")
                .experienceYears(5)
                .build();
        entityManager.persist(tempGuide);

        Reservation reservation = buildTestReservation(tempGuide);
        reservation.setStatus(status);
        return reservation;
    }

}