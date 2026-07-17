package com.property.property.application.service;

import com.property.property.application.dto.PropertyResponse;
import com.property.property.domain.model.PropertyId;
import com.property.property.domain.model.PropertySearchCriteria;
import com.property.property.domain.model.PropertySearchParams;
import com.property.property.domain.repository.PropertyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Optional<PropertyResponse> getPropertyById(Long id) {
        var result = propertyRepository.findById(new PropertyId(id))
                .map(PropertyResponse::from);
        log.info("getPropertyById [{}]: {}", id, result.isPresent() ? "found" : "not found");
        return result;
    }

    public List<PropertyResponse> searchProperties(PropertySearchParams params) {
        var results = propertyRepository.search(PropertySearchCriteria.from(params)).stream()
                .map(PropertyResponse::from)
                .toList();
        log.info("searchProperties found {} result(s) for params: {}", results.size(), params);
        return results;
    }

    public Optional<PropertyResponse> getPropertyByReferenceCode(String referenceCode) {
        var result = propertyRepository.findByReferenceCode(referenceCode)
                .map(PropertyResponse::from);
        log.info("getPropertyByReferenceCode [{}]: {}", referenceCode, result.isPresent() ? "found" : "not found");
        return result;
    }
}
