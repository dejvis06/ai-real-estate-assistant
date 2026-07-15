package com.reservation.reservation.application.dto;

import java.math.BigDecimal;

public record PropertyNearbyPlaceResponse(
        Long id,
        Long propertyId,
        String name,
        String type,
        BigDecimal distance,
        String distanceUnit,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
