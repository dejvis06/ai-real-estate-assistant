package com.property.property.domain.repository;

import com.property.property.domain.model.Property;
import com.property.property.domain.model.PropertyId;
import com.property.property.domain.model.PropertySearchCriteria;

import java.util.List;
import java.util.Optional;

public interface PropertyRepository {

    Optional<Property> findById(PropertyId id);

    Optional<Property> findByReferenceCode(String referenceCode);

    List<Property> findByCriteria(PropertySearchCriteria criteria);
}
