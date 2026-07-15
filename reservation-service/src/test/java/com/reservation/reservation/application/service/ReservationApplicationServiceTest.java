package com.reservation.reservation.application.service;

import com.reservation.reservation.application.dto.CreateReservationRequest;
import com.reservation.reservation.application.dto.PropertyResponse;
import com.reservation.reservation.application.dto.ReservationDetailsResponse;
import com.reservation.reservation.application.port.PropertyServiceClient;
import com.reservation.reservation.domain.model.Reservation;
import com.reservation.reservation.domain.model.ReservationId;
import com.reservation.reservation.domain.model.ReservationSchedule;
import com.reservation.reservation.domain.model.ReservationStatus;
import com.reservation.reservation.domain.repository.ReservationRepository;
import com.reservation.reservation.domain.service.ReservationNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationApplicationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private PropertyServiceClient propertyServiceClient;

    @Mock
    private ReservationNumberGenerator reservationNumberGenerator;

    private ReservationApplicationService service;

    @BeforeEach
    void setUp() {
        service = new ReservationApplicationService(
                reservationRepository, propertyServiceClient, reservationNumberGenerator);
    }

    @Test
    void getReservationDetails_existingId_returnsDetails() {
        Reservation reservation = buildReservation(ReservationStatus.CONFIRMED);
        when(reservationRepository.findById(new ReservationId(1L))).thenReturn(Optional.of(reservation));
        when(propertyServiceClient.findById(1L)).thenReturn(Optional.empty());

        ReservationDetailsResponse result = service.getReservationDetails(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.status()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(result.policy()).isNotNull();
        assertThat(result.schedule()).isNotNull();
    }

    @Test
    void getReservationDetails_propertyFound_includesPropertyInResponse() {
        Reservation reservation = buildReservation(ReservationStatus.CONFIRMED);
        PropertyResponse property = new PropertyResponse(
                1L, "PROP-1001", "Modern Apartment", null,
                "APARTMENT", "SALE", "AVAILABLE",
                null, "EUR", 2, 1, null, null, null, null,
                "Street 1", "Tirana", "Albania",
                null, null, null, null, null, null, null, null);

        when(reservationRepository.findById(new ReservationId(1L))).thenReturn(Optional.of(reservation));
        when(propertyServiceClient.findById(1L)).thenReturn(Optional.of(property));

        ReservationDetailsResponse result = service.getReservationDetails(1L);

        assertThat(result.property()).isNotNull();
        assertThat(result.property().referenceCode()).isEqualTo("PROP-1001");
    }

    @Test
    void getReservationDetails_notFound_throwsException() {
        when(reservationRepository.findById(new ReservationId(99L))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getReservationDetails(99L))
                .isInstanceOf(ReservationNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createReservation_validRequest_savesAndReturnsDetails() {
        when(reservationNumberGenerator.generate()).thenReturn("RES-2026-1001");
        when(reservationRepository.save(any())).thenAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            return Reservation.builder()
                    .id(new ReservationId(1L))
                    .reservationNumber(r.getReservationNumber())
                    .status(r.getStatus())
                    .propertyId(r.getPropertyId())
                    .customerName(r.getCustomerName())
                    .customerEmail(r.getCustomerEmail())
                    .schedule(r.getSchedule())
                    .createdAt(r.getCreatedAt())
                    .updatedAt(r.getUpdatedAt())
                    .build();
        });
        when(propertyServiceClient.findById(any())).thenReturn(Optional.empty());

        CreateReservationRequest request = new CreateReservationRequest(
                1L, "John Smith", "john@example.com", null,
                LocalDateTime.now().plusDays(5), 60, "Interested in buying.");

        ReservationDetailsResponse result = service.createReservation(request);

        assertThat(result.reservationNumber()).isEqualTo("RES-2026-1001");
        assertThat(result.status()).isEqualTo(ReservationStatus.PENDING);
    }

    // -------------------------------------------------------------------------

    private Reservation buildReservation(ReservationStatus status) {
        return Reservation.builder()
                .id(new ReservationId(1L))
                .reservationNumber("RES-2026-0001")
                .status(status)
                .propertyId(1L)
                .customerName("John Smith")
                .customerEmail("john@example.com")
                .schedule(new ReservationSchedule(LocalDateTime.now().plusDays(3), 60, null))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
