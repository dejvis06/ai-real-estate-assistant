package com.reservation.reservation.application.dto;

import java.time.LocalDateTime;

public record PropertyImageResponse(
        Long id,
        Long propertyId,
        String url,
        Integer displayOrder,
        Boolean isPrimary,
        LocalDateTime createdAt
) {
}
