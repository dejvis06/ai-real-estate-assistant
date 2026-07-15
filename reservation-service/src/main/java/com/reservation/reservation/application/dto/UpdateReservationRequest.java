package com.reservation.reservation.application.dto;

import com.reservation.reservation.domain.model.ReservationStatus;

import java.time.LocalDateTime;

public record UpdateReservationRequest(
        ReservationStatus status,
        LocalDateTime viewingDateTime,
        Integer durationMinutes,
        String agentNotes,
        String internalNotes
) {
}
