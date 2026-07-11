package com.property.property.domain.model;

import java.math.BigDecimal;

public record PropertySearchCriteria(
        String city,
        PropertyType propertyType,
        ListingType listingType,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Integer minBedrooms
) {}
