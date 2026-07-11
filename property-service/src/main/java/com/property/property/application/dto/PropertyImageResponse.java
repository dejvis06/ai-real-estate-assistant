package com.property.property.application.dto;

import com.property.property.domain.model.PropertyImage;

import java.time.LocalDateTime;

public record PropertyImageResponse(
        Long id,
        String url,
        int displayOrder,
        boolean primary,
        LocalDateTime createdAt
) {

    public static PropertyImageResponse from(PropertyImage image) {
        return new PropertyImageResponse(
                image.getId(),
                image.getUrl(),
                image.getDisplayOrder(),
                image.isPrimary(),
                image.getCreatedAt()
        );
    }
}
