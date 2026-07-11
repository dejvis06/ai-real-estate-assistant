package com.property.property.infrastructure.persistence.repository;

import com.property.property.domain.model.PropertySearchCriteria;
import com.property.property.domain.model.PropertyStatus;
import com.property.property.infrastructure.persistence.entity.PropertyJpaEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PropertySpecification {

    private PropertySpecification() {}

    public static Specification<PropertyJpaEntity> byCriteria(PropertySearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("status"), PropertyStatus.AVAILABLE));

            if (criteria.city() != null && !criteria.city().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("city")),
                        "%" + criteria.city().toLowerCase() + "%"
                ));
            }
            if (criteria.propertyType() != null) {
                predicates.add(cb.equal(root.get("propertyType"), criteria.propertyType()));
            }
            if (criteria.listingType() != null) {
                predicates.add(cb.equal(root.get("listingType"), criteria.listingType()));
            }
            if (criteria.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), criteria.minPrice()));
            }
            if (criteria.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), criteria.maxPrice()));
            }
            if (criteria.minBedrooms() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("bedrooms"), criteria.minBedrooms()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
