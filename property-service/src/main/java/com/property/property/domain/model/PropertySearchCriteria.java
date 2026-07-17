package com.property.property.domain.model;

import java.math.BigDecimal;

public record PropertySearchCriteria(

        String title,
        String description,
        PropertyType propertyType,
        ListingType listingType,
        PropertyStatus status,

        String city,
        String country,

        BigDecimal minPrice,
        BigDecimal maxPrice,

        Integer minBedrooms,
        Integer maxBedrooms,

        Integer minBathrooms,
        Integer maxBathrooms,

        BigDecimal minArea,
        BigDecimal maxArea,

        Integer minFloor,
        Integer maxFloor,

        Integer minYearBuilt,
        Integer maxYearBuilt
) {

    public static PropertySearchCriteria from(PropertySearchParams params) {
        return new PropertySearchCriteria(
                params.title(),
                params.description(),
                params.propertyType() != null ? PropertyType.valueOf(params.propertyType().toUpperCase()) : null,
                params.listingType() != null ? ListingType.valueOf(params.listingType().toUpperCase()) : null,
                params.status() != null ? PropertyStatus.valueOf(params.status().toUpperCase()) : null,
                params.city(),
                params.country(),
                params.minPrice(),
                params.maxPrice(),
                params.minBedrooms(),
                params.maxBedrooms(),
                params.minBathrooms(),
                params.maxBathrooms(),
                params.minArea(),
                params.maxArea(),
                params.minFloor(),
                params.maxFloor(),
                params.minYearBuilt(),
                params.maxYearBuilt()
        );
    }
}
