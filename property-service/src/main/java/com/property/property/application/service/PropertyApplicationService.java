package com.property.property.application.service;

import com.property.property.application.dto.PropertyResponse;
import com.property.property.domain.model.ListingType;
import com.property.property.domain.model.PropertySearchCriteria;
import com.property.property.domain.model.PropertyType;
import com.property.property.domain.repository.PropertyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PropertyApplicationService {

    private static final Logger log = LoggerFactory.getLogger(PropertyApplicationService.class);

    private final PropertyRepository propertyRepository;

    public PropertyApplicationService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    public List<PropertyResponse> searchProperties(String city, String propertyType, String listingType,
                                                   BigDecimal minPrice, BigDecimal maxPrice, Integer minBedrooms) {
        var criteria = new PropertySearchCriteria(
                city,
                propertyType != null ? PropertyType.valueOf(propertyType.toUpperCase()) : null,
                listingType != null ? ListingType.valueOf(listingType.toUpperCase()) : null,
                minPrice,
                maxPrice,
                minBedrooms
        );
        var results = propertyRepository.findByCriteria(criteria).stream()
                .map(PropertyResponse::from)
                .toList();
        log.info("searchProperties found {} result(s) for criteria: {}", results.size(), criteria);
        return results;
    }

    public Optional<PropertyResponse> getPropertyByReferenceCode(String referenceCode) {
        var result = propertyRepository.findByReferenceCode(referenceCode)
                .map(PropertyResponse::from);
        log.info("getPropertyByReferenceCode [{}]: {}", referenceCode, result.isPresent() ? "found" : "not found");
        return result;
    }
}
