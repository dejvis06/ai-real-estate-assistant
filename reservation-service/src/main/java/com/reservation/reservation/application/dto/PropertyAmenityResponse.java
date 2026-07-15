package com.reservation.reservation.application.dto;

import java.time.LocalDateTime;

public record PropertyAmenityResponse(
        Long id,
        Long propertyId,
        String name,
        LocalDateTime createdAt
) {
}
