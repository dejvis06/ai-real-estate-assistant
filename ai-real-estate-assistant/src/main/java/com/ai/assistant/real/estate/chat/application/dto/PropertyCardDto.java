package com.ai.assistant.real.estate.chat.application.dto;

import java.util.List;

public record PropertyCardDto(
        String referenceCode,
        String title,
        String description,
        String propertyType,
        String listingType,
        String status,
        Double price,
        String currency,
        Integer bedrooms,
        Integer bathrooms,
        Double area,
        String city,
        String country,
        String address,
        List<String> imageUrls,
        List<String> amenities
) {}
