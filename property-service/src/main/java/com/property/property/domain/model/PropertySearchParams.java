package com.property.property.domain.model;

import java.math.BigDecimal;

public record PropertySearchParams(

        String title,
        String description,
        String propertyType,
        String listingType,
        String status,

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
) {}
