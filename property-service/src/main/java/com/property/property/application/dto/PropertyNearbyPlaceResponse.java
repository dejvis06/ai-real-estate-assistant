package com.property.property.application.dto;

import com.property.property.domain.model.PropertyNearbyPlace;

import java.math.BigDecimal;

public record PropertyNearbyPlaceResponse(
        Long id,
        String name,
        String type,
        BigDecimal distance,
        String distanceUnit,
        BigDecimal latitude,
        BigDecimal longitude
) {

    public static PropertyNearbyPlaceResponse from(PropertyNearbyPlace place) {
        return new PropertyNearbyPlaceResponse(
                place.getId(),
                place.getName(),
                place.getType(),
                place.getDistance(),
                place.getDistanceUnit(),
                place.getLatitude(),
                place.getLongitude()
        );
    }
}
