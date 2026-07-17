package com.property.property.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record PropertySearchParams(

        @JsonProperty String title,
        @JsonProperty String description,
        @JsonProperty String propertyType,
        @JsonProperty String listingType,
        @JsonProperty String status,

        @JsonProperty String city,
        @JsonProperty String country,

        @JsonProperty BigDecimal minPrice,
        @JsonProperty BigDecimal maxPrice,

        @JsonProperty Integer minBedrooms,
        @JsonProperty Integer maxBedrooms,

        @JsonProperty Integer minBathrooms,
        @JsonProperty Integer maxBathrooms,

        @JsonProperty BigDecimal minArea,
        @JsonProperty BigDecimal maxArea,

        @JsonProperty Integer minFloor,
        @JsonProperty Integer maxFloor,

        @JsonProperty Integer minYearBuilt,
        @JsonProperty Integer maxYearBuilt
) {}
