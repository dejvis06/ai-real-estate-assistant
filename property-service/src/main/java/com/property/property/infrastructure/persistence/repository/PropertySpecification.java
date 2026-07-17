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

    public static Specification<PropertyJpaEntity> byCriteria(PropertySearchCriteria c) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("status"),
                    c.status() != null ? c.status() : PropertyStatus.AVAILABLE));

            if (c.title() != null && !c.title().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + c.title().toLowerCase() + "%"));
            }
            if (c.description() != null && !c.description().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("description")), "%" + c.description().toLowerCase() + "%"));
            }
            if (c.propertyType() != null) {
                predicates.add(cb.equal(root.get("propertyType"), c.propertyType()));
            }
            if (c.listingType() != null) {
                predicates.add(cb.equal(root.get("listingType"), c.listingType()));
            }
            if (c.city() != null && !c.city().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("city")), "%" + c.city().toLowerCase() + "%"));
            }
            if (c.country() != null && !c.country().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("country")), "%" + c.country().toLowerCase() + "%"));
            }
            if (c.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), c.minPrice()));
            }
            if (c.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), c.maxPrice()));
            }
            if (c.minBedrooms() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("bedrooms"), c.minBedrooms()));
            }
            if (c.maxBedrooms() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("bedrooms"), c.maxBedrooms()));
            }
            if (c.minBathrooms() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("bathrooms"), c.minBathrooms()));
            }
            if (c.maxBathrooms() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("bathrooms"), c.maxBathrooms()));
            }
            if (c.minArea() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("area"), c.minArea()));
            }
            if (c.maxArea() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("area"), c.maxArea()));
            }
            if (c.minFloor() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("floor"), c.minFloor()));
            }
            if (c.maxFloor() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("floor"), c.maxFloor()));
            }
            if (c.minYearBuilt() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("yearBuilt"), c.minYearBuilt()));
            }
            if (c.maxYearBuilt() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("yearBuilt"), c.maxYearBuilt()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
