package com.property.property.infrastructure.persistence.mapper;

import com.property.property.domain.model.*;
import com.property.property.infrastructure.persistence.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PropertyMapperTest {

    private PropertyMapper mapper;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        mapper = new PropertyMapper();
    }

    @Test
    void toDomain_shouldMapAllScalarFields() {
        PropertyJpaEntity entity = buildPropertyEntity();

        Property property = mapper.toDomain(entity);

        assertThat(property.getId().value()).isEqualTo(1L);
        assertThat(property.getReferenceCode()).isEqualTo("PROP-TEST");
        assertThat(property.getTitle()).isEqualTo("Test Title");
        assertThat(property.getDescription()).isEqualTo("Test Description");
        assertThat(property.getPropertyType()).isEqualTo(PropertyType.APARTMENT);
        assertThat(property.getListingType()).isEqualTo(ListingType.SALE);
        assertThat(property.getStatus()).isEqualTo(PropertyStatus.AVAILABLE);
        assertThat(property.getPrice()).isEqualByComparingTo(new BigDecimal("185000.00"));
        assertThat(property.getCurrency()).isEqualTo("EUR");
        assertThat(property.getBedrooms()).isEqualTo(2);
        assertThat(property.getBathrooms()).isEqualTo(1);
        assertThat(property.getCity()).isEqualTo("Tirana");
        assertThat(property.getCountry()).isEqualTo("Albania");
        assertThat(property.getCreatedAt()).isEqualTo(now);
        assertThat(property.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toDomain_withImages_shouldMapImages() {
        PropertyJpaEntity entity = buildPropertyEntity();
        var img = buildImageEntity(entity);
        entity.setImages(Set.of(img));

        Property property = mapper.toDomain(entity);

        assertThat(property.getImages()).hasSize(1);
        PropertyImage image = property.getImages().get(0);
        assertThat(image.getUrl()).isEqualTo("https://example.com/img.jpg");
        assertThat(image.getDisplayOrder()).isEqualTo(0);
        assertThat(image.isPrimary()).isTrue();
    }

    @Test
    void toDomain_withAmenities_shouldMapAmenities() {
        PropertyJpaEntity entity = buildPropertyEntity();
        var amenity = buildAmenityEntity(entity, "Pool");
        entity.setAmenities(Set.of(amenity));

        Property property = mapper.toDomain(entity);

        assertThat(property.getAmenities()).hasSize(1);
        assertThat(property.getAmenities().get(0).getName()).isEqualTo("Pool");
    }

    @Test
    void toDomain_withNearbyPlaces_shouldMapNearbyPlaces() {
        PropertyJpaEntity entity = buildPropertyEntity();
        var place = buildNearbyPlaceEntity(entity, "School", "School");
        entity.setNearbyPlaces(Set.of(place));

        Property property = mapper.toDomain(entity);

        assertThat(property.getNearbyPlaces()).hasSize(1);
        PropertyNearbyPlace np = property.getNearbyPlaces().get(0);
        assertThat(np.getName()).isEqualTo("School");
        assertThat(np.getType()).isEqualTo("School");
        assertThat(np.getDistance()).isEqualByComparingTo(new BigDecimal("0.5"));
        assertThat(np.getDistanceUnit()).isEqualTo("km");
    }

    @Test
    void toDomain_withEmptyCollections_shouldReturnEmptyLists() {
        PropertyJpaEntity entity = buildPropertyEntity();

        Property property = mapper.toDomain(entity);

        assertThat(property.getImages()).isEmpty();
        assertThat(property.getAmenities()).isEmpty();
        assertThat(property.getNearbyPlaces()).isEmpty();
    }

    @Test
    void toDomain_resultImages_shouldBeUnmodifiable() {
        PropertyJpaEntity entity = buildPropertyEntity();
        entity.setImages(Set.of(buildImageEntity(entity)));

        Property property = mapper.toDomain(entity);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> property.getImages().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private PropertyJpaEntity buildPropertyEntity() {
        var entity = new PropertyJpaEntity();
        entity.setId(1L);
        entity.setReferenceCode("PROP-TEST");
        entity.setTitle("Test Title");
        entity.setDescription("Test Description");
        entity.setPropertyType(PropertyType.APARTMENT);
        entity.setListingType(ListingType.SALE);
        entity.setStatus(PropertyStatus.AVAILABLE);
        entity.setPrice(new BigDecimal("185000.00"));
        entity.setCurrency("EUR");
        entity.setBedrooms(2);
        entity.setBathrooms(1);
        entity.setCity("Tirana");
        entity.setCountry("Albania");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return entity;
    }

    private PropertyImageJpaEntity buildImageEntity(PropertyJpaEntity parent) {
        var img = new PropertyImageJpaEntity();
        img.setId(10L);
        img.setProperty(parent);
        img.setUrl("https://example.com/img.jpg");
        img.setDisplayOrder(0);
        img.setPrimary(true);
        img.setCreatedAt(now);
        return img;
    }

    private PropertyAmenityJpaEntity buildAmenityEntity(PropertyJpaEntity parent, String name) {
        var amenity = new PropertyAmenityJpaEntity();
        amenity.setId(20L);
        amenity.setProperty(parent);
        amenity.setName(name);
        amenity.setCreatedAt(now);
        return amenity;
    }

    private PropertyNearbyPlaceJpaEntity buildNearbyPlaceEntity(PropertyJpaEntity parent, String name, String type) {
        var place = new PropertyNearbyPlaceJpaEntity();
        place.setId(30L);
        place.setProperty(parent);
        place.setName(name);
        place.setType(type);
        place.setDistance(new BigDecimal("0.5"));
        place.setDistanceUnit("km");
        place.setLatitude(new BigDecimal("41.32"));
        place.setLongitude(new BigDecimal("19.81"));
        return place;
    }
}
