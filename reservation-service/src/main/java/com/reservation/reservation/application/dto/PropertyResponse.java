package com.reservation.reservation.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Mirror of the PropertyResponse from property-service.
 * Populated via REST call to the Property Service.
 */
public record PropertyResponse(
        Long id,
        String referenceCode,
        String title,
        String description,
        String propertyType,
        String listingType,
        String status,
        BigDecimal price,
        String currency,
        Integer bedrooms,
        Integer bathrooms,
        BigDecimal area,
        Integer floor,
        Integer totalFloors,
        Integer yearBuilt,
        String address,
        String city,
        String country,
        BigDecimal latitude,
        BigDecimal longitude,
        Long agentId,
        List<PropertyImageResponse> images,
        List<PropertyAmenityResponse> amenities,
        List<PropertyNearbyPlaceResponse> nearbyPlaces,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
