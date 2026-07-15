package com.reservation.reservation.infrastructure.persistence.mapper;

import com.reservation.reservation.domain.model.Reservation;
import com.reservation.reservation.domain.model.ReservationId;
import com.reservation.reservation.domain.model.ReservationSchedule;
import com.reservation.reservation.infrastructure.persistence.entity.ReservationJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {

    public Reservation toDomain(ReservationJpaEntity entity) {
        ReservationSchedule schedule = new ReservationSchedule(
                entity.getViewingDateTime(),
                entity.getDurationMinutes(),
                entity.getAgentNotes()
        );

        return Reservation.builder()
                .id(new ReservationId(entity.getId()))
                .reservationNumber(entity.getReservationNumber())
                .status(entity.getStatus())
                .propertyId(entity.getPropertyId())
                .customerName(entity.getCustomerName())
                .customerEmail(entity.getCustomerEmail())
                .customerPhone(entity.getCustomerPhone())
                .schedule(schedule)
                .customerMessage(entity.getCustomerMessage())
                .internalNotes(entity.getInternalNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public ReservationJpaEntity toEntity(Reservation domain) {
        ReservationJpaEntity entity = new ReservationJpaEntity();
        if (domain.getId() != null) {
            entity.setId(domain.getId().value());
        }
        entity.setReservationNumber(domain.getReservationNumber());
        entity.setStatus(domain.getStatus());
        entity.setPropertyId(domain.getPropertyId());
        entity.setCustomerName(domain.getCustomerName());
        entity.setCustomerEmail(domain.getCustomerEmail());
        entity.setCustomerPhone(domain.getCustomerPhone());
        if (domain.getSchedule() != null) {
            entity.setViewingDateTime(domain.getSchedule().getViewingDateTime());
            entity.setDurationMinutes(domain.getSchedule().getDurationMinutes());
            entity.setAgentNotes(domain.getSchedule().getAgentNotes());
        }
        entity.setCustomerMessage(domain.getCustomerMessage());
        entity.setInternalNotes(domain.getInternalNotes());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
