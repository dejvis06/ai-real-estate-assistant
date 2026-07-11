package com.property.property.infrastructure.persistence.repository;

import com.property.property.infrastructure.persistence.entity.PropertyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PropertyJpaRepository
        extends JpaRepository<PropertyJpaEntity, Long>, JpaSpecificationExecutor<PropertyJpaEntity> {

    @Query("""
            SELECT p FROM PropertyJpaEntity p
            LEFT JOIN FETCH p.images
            LEFT JOIN FETCH p.amenities
            LEFT JOIN FETCH p.nearbyPlaces
            WHERE p.referenceCode = :referenceCode
            """)
    Optional<PropertyJpaEntity> findByReferenceCodeWithDetails(@Param("referenceCode") String referenceCode);

    @Query("""
            SELECT DISTINCT p FROM PropertyJpaEntity p
            LEFT JOIN FETCH p.images
            LEFT JOIN FETCH p.amenities
            LEFT JOIN FETCH p.nearbyPlaces
            WHERE p.id IN :ids
            """)
    java.util.List<PropertyJpaEntity> findAllWithDetailsByIdIn(@Param("ids") java.util.List<Long> ids);
}
