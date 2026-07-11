package com.property.property.infrastructure.persistence.mapper;

import com.property.property.domain.model.*;
import com.property.property.infrastructure.persistence.entity.*;
import org.springframework.stereotype.Component;

@Component
public class PropertyMapper {

    public Property toDomain(PropertyJpaEntity entity) {
        return Property.builder()
                .id(new PropertyId(entity.getId()))
                .referenceCode(entity.getReferenceCode())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .propertyType(entity.getPropertyType())
                .listingType(entity.getListingType())
                .status(entity.getStatus())
                .price(entity.getPrice())
                .currency(entity.getCurrency())
                .bedrooms(entity.getBedrooms())
                .bathrooms(entity.getBathrooms())
                .area(entity.getArea())
                .floor(entity.getFloor())
                .totalFloors(entity.getTotalFloors())
                .yearBuilt(entity.getYearBuilt())
                .address(entity.getAddress())
                .city(entity.getCity())
                .country(entity.getCountry())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .agentId(entity.getAgentId())
                .images(entity.getImages().stream().map(this::toImageDomain).toList())
                .amenities(entity.getAmenities().stream().map(this::toAmenityDomain).toList())
                .nearbyPlaces(entity.getNearbyPlaces().stream().map(this::toNearbyPlaceDomain).toList())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private PropertyImage toImageDomain(PropertyImageJpaEntity entity) {
        return new PropertyImage(
                entity.getId(),
                entity.getUrl(),
                entity.getDisplayOrder(),
                entity.isPrimary(),
                entity.getCreatedAt()
        );
    }

    private PropertyAmenity toAmenityDomain(PropertyAmenityJpaEntity entity) {
        return new PropertyAmenity(entity.getId(), entity.getName(), entity.getCreatedAt());
    }

    private PropertyNearbyPlace toNearbyPlaceDomain(PropertyNearbyPlaceJpaEntity entity) {
        return new PropertyNearbyPlace(
                entity.getId(),
                entity.getName(),
                entity.getType(),
                entity.getDistance(),
                entity.getDistanceUnit(),
                entity.getLatitude(),
                entity.getLongitude()
        );
    }
}
