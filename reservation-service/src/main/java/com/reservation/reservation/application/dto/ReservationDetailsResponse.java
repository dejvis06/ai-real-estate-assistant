package com.reservation.reservation.application.dto;

import com.reservation.reservation.domain.model.Reservation;
import com.reservation.reservation.domain.model.ReservationPolicy;
import com.reservation.reservation.domain.model.ReservationStatus;

import java.time.LocalDateTime;

public record ReservationDetailsResponse(
        Long id,
        String reservationNumber,
        ReservationStatus status,
        PropertyResponse property,
        ReservationScheduleResponse schedule,
        ReservationPolicyResponse policy,
        String customerMessage,
        String internalNotes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static ReservationDetailsResponse from(Reservation reservation,
                                                   PropertyResponse property,
                                                   ReservationPolicy policy) {
        return new ReservationDetailsResponse(
                reservation.getId().value(),
                reservation.getReservationNumber(),
                reservation.getStatus(),
                property,
                ReservationScheduleResponse.from(reservation.getSchedule()),
                ReservationPolicyResponse.from(policy),
                reservation.getCustomerMessage(),
                reservation.getInternalNotes(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt()
        );
    }
}
