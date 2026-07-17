package com.property.property.infrastructure.persistence.repository;

import com.property.property.domain.model.Property;
import com.property.property.domain.model.PropertyId;
import com.property.property.domain.model.PropertySearchCriteria;
import com.property.property.domain.repository.PropertyRepository;
import com.property.property.infrastructure.persistence.entity.PropertyJpaEntity;
import com.property.property.infrastructure.persistence.mapper.PropertyMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PropertyRepositoryImpl implements PropertyRepository {

    private final PropertyJpaRepository propertyJpaRepository;
    private final PropertyMapper propertyMapper;

    public PropertyRepositoryImpl(PropertyJpaRepository propertyJpaRepository, PropertyMapper propertyMapper) {
        this.propertyJpaRepository = propertyJpaRepository;
        this.propertyMapper = propertyMapper;
    }

    @Override
    public Optional<Property> findById(PropertyId id) {
        return propertyJpaRepository.findById(id.value())
                .map(propertyMapper::toDomain);
    }

    @Override
    public Optional<Property> findByReferenceCode(String referenceCode) {
        return propertyJpaRepository.findByReferenceCodeWithDetails(referenceCode)
                .map(propertyMapper::toDomain);
    }

    @Override
    public List<Property> search(PropertySearchCriteria criteria) {
        var matchingIds = propertyJpaRepository.findAll(PropertySpecification.byCriteria(criteria))
                .stream()
                .map(PropertyJpaEntity::getId)
                .toList();

        if (matchingIds.isEmpty()) {
            return List.of();
        }

        return propertyJpaRepository.findAllWithDetailsByIdIn(matchingIds).stream()
                .map(propertyMapper::toDomain)
                .toList();
    }
}
