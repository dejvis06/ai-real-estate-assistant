package com.property.property.application.dto;

import com.property.property.domain.model.PropertyAmenity;

public record PropertyAmenityResponse(Long id, String name) {

    public static PropertyAmenityResponse from(PropertyAmenity amenity) {
        return new PropertyAmenityResponse(amenity.getId(), amenity.getName());
    }
}
