package com.desertakal.desertakal;

import com.desertakal.desertakal.exception.custom.BadRequestException;
import com.desertakal.desertakal.exception.custom.BusinessRuleException;
import com.desertakal.desertakal.exception.custom.GuideNotAvailableException;
import com.desertakal.desertakal.exception.custom.UnauthorizedActionException;
import com.desertakal.desertakal.model.dto.reservation.ReservationCreateDTO;
import com.desertakal.desertakal.model.dto.reservation.ReservationFindDTO;
import com.desertakal.desertakal.model.dto.reservation.ReservationUpdateDTO;
import com.desertakal.desertakal.model.entity.Guide;
import com.desertakal.desertakal.model.entity.Reservation;
import com.desertakal.desertakal.model.entity.Tour;
import com.desertakal.desertakal.model.entity.Tourist;
import com.desertakal.desertakal.model.enums.ReservationStatus;
import com.desertakal.desertakal.model.mapper.ReservationMapper;
import com.desertakal.desertakal.repository.GuideRepository;
import com.desertakal.desertakal.repository.ReservationRepository;
import com.desertakal.desertakal.repository.TourRepository;
import com.desertakal.desertakal.repository.TouristRepository;
import com.desertakal.desertakal.service.impl.ReservationServiceImpl;
import com.desertakal.desertakal.service.interfaces.DocumentGeneratorService;
import com.desertakal.desertakal.service.interfaces.FileStorageService;
import com.desertakal.desertakal.service.interfaces.NotificationService;
import com.desertakal.desertakal.service.interfaces.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    ReservationServiceImpl reservationService;

    @Mock
    ReservationRepository repository;
    @Mock
    ReservationMapper mapper;
    @Mock
    TouristRepository touristRepository;
    @Mock
    GuideRepository guideRepository;
    @Mock
    TourRepository tourRepository;
    @Mock
    NotificationService notificationService;
    @Mock
    DocumentGeneratorService documentGeneratorService;
    @Mock
    FileStorageService fileStorageService;
    @Mock
    PaymentService paymentService;

    UUID TEST_TOURIST_UUID = UUID.randomUUID();
    UUID TEST_GUIDE_UUID = UUID.randomUUID();
    UUID TEST_TOUR_UUID = UUID.randomUUID();
    UUID TEST_RESERVATION_UUID = UUID.randomUUID();

    Tourist testTourist;
    Guide testGuide;
    Tour testTour;
    Reservation testReservation;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationServiceImpl(
                repository, mapper, touristRepository, guideRepository, tourRepository,
                notificationService, documentGeneratorService, fileStorageService, paymentService
        );

        testTourist = Tourist.builder()
                .uuid(TEST_TOURIST_UUID)
                .firstName("Test")
                .lastName("Tourist")
                .build();

        testGuide = Guide.builder()
                .uuid(TEST_GUIDE_UUID)
                .firstName("Ahmed")
                .lastName("Benali")
                .build();

        testTour = Tour.builder()
                .uuid(TEST_TOUR_UUID)
                .title("Sahara 3 Days Tour")
                .durationDays(3)
                .build();

        testReservation = Reservation.builder()
                .uuid(TEST_RESERVATION_UUID)
                .tourist(testTourist)
                .guide(testGuide)
                .tour(testTour)
                .status(ReservationStatus.PENDING)
                .startDate(LocalDateTime.now().plusWeeks(2))
                .build();
    }


    @Test
    @DisplayName("create() - Normal state: Booking created successfully")
    void create_success() {
        ReservationCreateDTO dto = buildValidCreateDto();

        when(touristRepository.findByUuid(TEST_TOURIST_UUID)).thenReturn(Optional.of(testTourist));
        when(touristRepository.hasReservationsWithStatuses(any(), any())).thenReturn(false);
        when(guideRepository.findByUuid(TEST_GUIDE_UUID)).thenReturn(Optional.of(testGuide));
        when(tourRepository.findByUuid(TEST_TOUR_UUID)).thenReturn(Optional.of(testTour));
        when(guideRepository.isGuideAvailable(any(), any(), any())).thenReturn(true);
        when(mapper.toEntity(dto)).thenReturn(testReservation);
        when(repository.save(testReservation)).thenReturn(testReservation);
        when(mapper.toFindDto(testReservation)).thenReturn(new ReservationFindDTO());

        ReservationFindDTO result = reservationService.create(dto, TEST_TOURIST_UUID);

        assertThat(result).isNotNull();

        verify(repository).save(testReservation);
        verify(documentGeneratorService).generateConfirmationAssets(testReservation);

        verify(notificationService, times(2)).create(anyString(), anyString(), any(UUID.class));
    }

    @Test
    @DisplayName("create() - Rejected: Tourists already have an active booking")
    void create_rejected_alreadyActiveReservation() {
        ReservationCreateDTO dto = buildValidCreateDto();

        when(touristRepository.findByUuid(TEST_TOURIST_UUID)).thenReturn(Optional.of(testTourist));
        when(touristRepository.hasReservationsWithStatuses(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> reservationService.create(dto, TEST_TOURIST_UUID))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already have an active reservation");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create() - Rejected: Booking date less than 7 days")
    void create_rejected_dateTooClose() {
        ReservationCreateDTO dto = buildValidCreateDto();
        dto.setStartDate(LocalDateTime.now().plusDays(3));

        when(touristRepository.findByUuid(TEST_TOURIST_UUID)).thenReturn(Optional.of(testTourist));
        when(touristRepository.hasReservationsWithStatuses(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> reservationService.create(dto, TEST_TOURIST_UUID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("at least one week in advance");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create() - Rejected: Guide is busy during the same period")
    void create_rejected_guideNotAvailable() {
        ReservationCreateDTO dto = buildValidCreateDto();

        when(touristRepository.findByUuid(TEST_TOURIST_UUID)).thenReturn(Optional.of(testTourist));
        when(touristRepository.hasReservationsWithStatuses(any(), any())).thenReturn(false);
        when(guideRepository.findByUuid(TEST_GUIDE_UUID)).thenReturn(Optional.of(testGuide));
        when(tourRepository.findByUuid(TEST_TOUR_UUID)).thenReturn(Optional.of(testTour));
        when(guideRepository.isGuideAvailable(any(), any(), any())).thenReturn(false);

        assertThatThrownBy(() -> reservationService.create(dto, TEST_TOURIST_UUID))
                .isInstanceOf(GuideNotAvailableException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("cancel() - Normal state: Cancelled successfully by booking owner")
    void cancel_successByOwner() {
        when(repository.findByUuid(TEST_RESERVATION_UUID)).thenReturn(Optional.of(testReservation));

        reservationService.cancel(TEST_RESERVATION_UUID, TEST_TOURIST_UUID, false);

        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);
        verify(repository).save(reservationCaptor.capture());

        assertThat(reservationCaptor.getValue().getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        verify(paymentService).processRefundOnCancel(testReservation, false);
        verify(notificationService, times(2)).create(anyString(), anyString(), any(UUID.class));
    }

    @Test
    @DisplayName("cancel() - Rejected: User is neither booking owner nor manager")
    void cancel_rejected_notAuthorized() {
        UUID otherUserUuid = UUID.randomUUID();

        when(repository.findByUuid(TEST_RESERVATION_UUID)).thenReturn(Optional.of(testReservation));

        assertThatThrownBy(() -> reservationService.cancel(TEST_RESERVATION_UUID, otherUserUuid, false))
                .isInstanceOf(UnauthorizedActionException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("cancel() - Rejected: Booking has already expired")
    void cancel_rejected_alreadyCompleted() {
        testReservation.setStatus(ReservationStatus.COMPLETED);
        when(repository.findByUuid(TEST_RESERVATION_UUID)).thenReturn(Optional.of(testReservation));

        assertThatThrownBy(() -> reservationService.cancel(TEST_RESERVATION_UUID, TEST_TOURIST_UUID, false))
                .isInstanceOf(BusinessRuleException.class);

        verify(repository, never()).save(any());
    }


    @Test
    @DisplayName("update() - Update start date: Automatically recalculates end date")
    void update_updateStartDate_autoRecalculateEndDate() {
        LocalDateTime newStartDate = LocalDateTime.now().plusWeeks(3);
        ReservationUpdateDTO dto = ReservationUpdateDTO.builder().startDate(newStartDate).build();

        when(repository.findByUuid(TEST_RESERVATION_UUID)).thenReturn(Optional.of(testReservation));
        when(guideRepository.isGuideAvailable(any(), any(), any())).thenReturn(true);

        reservationService.update(TEST_RESERVATION_UUID, dto, TEST_TOURIST_UUID);

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(repository).save(captor.capture());

        assertThat(captor.getValue().getEndDate()).isEqualTo(newStartDate.plusDays(3));
    }


    @Test
    @DisplayName("delete() - Automatically cleans files stored in MinIO")
    void delete_cleanupStoredFiles() {
        testReservation.setPdfUrl("voucher-abc123.pdf");
        testReservation.setQrCode("qr-abc123.png");

        when(repository.findByUuid(TEST_RESERVATION_UUID)).thenReturn(Optional.of(testReservation));

        reservationService.delete(TEST_RESERVATION_UUID);

        verify(fileStorageService).deleteFile("voucher-abc123.pdf");
        verify(fileStorageService).deleteFile("qr-abc123.png");
        verify(repository).delete(testReservation);
    }


    private ReservationCreateDTO buildValidCreateDto() {
        return ReservationCreateDTO.builder()
                .tourUuid(TEST_TOUR_UUID)
                .guideUuid(TEST_GUIDE_UUID)
                .startDate(LocalDateTime.now().plusWeeks(2))
                .numberPeople(2)
                .build();
    }

}