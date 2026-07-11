package com.property.property.application.dto;

import com.property.property.domain.model.Property;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    public static PropertyResponse from(Property property) {
        return new PropertyResponse(
                property.getId().value(),
                property.getReferenceCode(),
                property.getTitle(),
                property.getDescription(),
                property.getPropertyType().name(),
                property.getListingType().name(),
                property.getStatus().name(),
                property.getPrice(),
                property.getCurrency(),
                property.getBedrooms(),
                property.getBathrooms(),
                property.getArea(),
                property.getFloor(),
                property.getTotalFloors(),
                property.getYearBuilt(),
                property.getAddress(),
                property.getCity(),
                property.getCountry(),
                property.getLatitude(),
                property.getLongitude(),
                property.getAgentId(),
                property.getImages().stream().map(PropertyImageResponse::from).toList(),
                property.getAmenities().stream().map(PropertyAmenityResponse::from).toList(),
                property.getNearbyPlaces().stream().map(PropertyNearbyPlaceResponse::from).toList(),
                property.getCreatedAt(),
                property.getUpdatedAt()
        );
    }
}
