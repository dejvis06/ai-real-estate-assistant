package com.reservation.reservation.application.dto;

import java.time.LocalDateTime;

public record CreateReservationRequest(
        Long propertyId,
        String customerName,
        String customerEmail,
        String customerPhone,
        LocalDateTime viewingDateTime,
        Integer durationMinutes,
        String customerMessage
) {
}
