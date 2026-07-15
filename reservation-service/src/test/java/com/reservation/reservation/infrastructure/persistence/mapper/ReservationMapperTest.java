package com.reservation.reservation.infrastructure.persistence.mapper;

import com.reservation.reservation.domain.model.Reservation;
import com.reservation.reservation.domain.model.ReservationId;
import com.reservation.reservation.domain.model.ReservationSchedule;
import com.reservation.reservation.domain.model.ReservationStatus;
import com.reservation.reservation.infrastructure.persistence.entity.ReservationJpaEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationMapperTest {

    private final ReservationMapper mapper = new ReservationMapper();

    @Test
    void toDomain_mapsAllFields() {
        ReservationJpaEntity entity = new ReservationJpaEntity();
        entity.setId(1L);
        entity.setReservationNumber("RES-2026-0001");
        entity.setStatus(ReservationStatus.CONFIRMED);
        entity.setPropertyId(2L);
        entity.setCustomerName("Alice");
        entity.setCustomerEmail("alice@example.com");
        entity.setCustomerPhone("+355 69 111 2222");
        entity.setViewingDateTime(LocalDateTime.of(2026, 8, 10, 10, 0));
        entity.setDurationMinutes(60);
        entity.setAgentNotes("Meet at reception.");
        entity.setCustomerMessage("Interested in buying.");
        entity.setInternalNotes("VIP client.");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        Reservation domain = mapper.toDomain(entity);

        assertThat(domain.getId().value()).isEqualTo(1L);
        assertThat(domain.getReservationNumber()).isEqualTo("RES-2026-0001");
        assertThat(domain.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(domain.getPropertyId()).isEqualTo(2L);
        assertThat(domain.getCustomerName()).isEqualTo("Alice");
        assertThat(domain.getCustomerEmail()).isEqualTo("alice@example.com");
        assertThat(domain.getSchedule().getViewingDateTime()).isEqualTo(LocalDateTime.of(2026, 8, 10, 10, 0));
        assertThat(domain.getSchedule().getDurationMinutes()).isEqualTo(60);
        assertThat(domain.getSchedule().getAgentNotes()).isEqualTo("Meet at reception.");
        assertThat(domain.getCustomerMessage()).isEqualTo("Interested in buying.");
        assertThat(domain.getInternalNotes()).isEqualTo("VIP client.");
    }

    @Test
    void toEntity_mapsAllFields() {
        LocalDateTime viewingTime = LocalDateTime.of(2026, 8, 10, 10, 0);
        Reservation domain = Reservation.builder()
                .id(new ReservationId(1L))
                .reservationNumber("RES-2026-0001")
                .status(ReservationStatus.PENDING)
                .propertyId(3L)
                .customerName("Bob")
                .customerEmail("bob@example.com")
                .customerPhone("+44 7700 123456")
                .schedule(new ReservationSchedule(viewingTime, 45, "Ring doorbell."))
                .customerMessage("Just browsing.")
                .internalNotes(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ReservationJpaEntity entity = mapper.toEntity(domain);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getReservationNumber()).isEqualTo("RES-2026-0001");
        assertThat(entity.getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(entity.getPropertyId()).isEqualTo(3L);
        assertThat(entity.getCustomerName()).isEqualTo("Bob");
        assertThat(entity.getCustomerEmail()).isEqualTo("bob@example.com");
        assertThat(entity.getViewingDateTime()).isEqualTo(viewingTime);
        assertThat(entity.getDurationMinutes()).isEqualTo(45);
        assertThat(entity.getAgentNotes()).isEqualTo("Ring doorbell.");
        assertThat(entity.getCustomerMessage()).isEqualTo("Just browsing.");
    }

    @Test
    void toEntity_nullId_doesNotSetId() {
        Reservation domain = Reservation.builder()
                .reservationNumber("RES-2026-NEW")
                .status(ReservationStatus.PENDING)
                .propertyId(1L)
                .customerName("New Customer")
                .customerEmail("new@example.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ReservationJpaEntity entity = mapper.toEntity(domain);

        assertThat(entity.getId()).isNull();
    }
}
