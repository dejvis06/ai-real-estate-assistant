package com.property.property.application.service;

import com.property.property.application.dto.PropertyResponse;
import com.property.property.domain.model.*;
import com.property.property.domain.repository.PropertyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertyApplicationServiceTest {

    @Mock
    PropertyRepository propertyRepository;

    @InjectMocks
    PropertyApplicationService service;

    // ── searchProperties ─────────────────────────────────────────────────────

    @Test
    void searchProperties_withNoCriteria_shouldReturnAll() {
        when(propertyRepository.findByCriteria(any())).thenReturn(List.of(buildProperty("PROP-1001")));

        List<PropertyResponse> result = service.searchProperties(null, null, null, null, null, null);

        assertThat(result).hasSize(1);
    }

    @Test
    void searchProperties_shouldBuildCriteriaWithCorrectFields() {
        when(propertyRepository.findByCriteria(any())).thenReturn(List.of());

        service.searchProperties("Tirana", "APARTMENT", "SALE",
                new BigDecimal("100000"), new BigDecimal("200000"), 2);

        var captor = ArgumentCaptor.forClass(PropertySearchCriteria.class);
        verify(propertyRepository).findByCriteria(captor.capture());

        PropertySearchCriteria criteria = captor.getValue();
        assertThat(criteria.city()).isEqualTo("Tirana");
        assertThat(criteria.propertyType()).isEqualTo(PropertyType.APARTMENT);
        assertThat(criteria.listingType()).isEqualTo(ListingType.SALE);
        assertThat(criteria.minPrice()).isEqualTo(new BigDecimal("100000"));
        assertThat(criteria.maxPrice()).isEqualTo(new BigDecimal("200000"));
        assertThat(criteria.minBedrooms()).isEqualTo(2);
    }

    @Test
    void searchProperties_withLowercaseEnums_shouldParseCorrectly() {
        when(propertyRepository.findByCriteria(any())).thenReturn(List.of());

        service.searchProperties(null, "apartment", "rent", null, null, null);

        var captor = ArgumentCaptor.forClass(PropertySearchCriteria.class);
        verify(propertyRepository).findByCriteria(captor.capture());

        assertThat(captor.getValue().propertyType()).isEqualTo(PropertyType.APARTMENT);
        assertThat(captor.getValue().listingType()).isEqualTo(ListingType.RENT);
    }

    @Test
    void searchProperties_withInvalidPropertyType_shouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> service.searchProperties(null, "INVALID_TYPE", null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void searchProperties_shouldMapToResponse() {
        when(propertyRepository.findByCriteria(any()))
                .thenReturn(List.of(buildProperty("PROP-1001")));

        List<PropertyResponse> result = service.searchProperties(null, null, null, null, null, null);

        assertThat(result.get(0).referenceCode()).isEqualTo("PROP-1001");
        assertThat(result.get(0).city()).isEqualTo("Tirana");
    }

    // ── getPropertyByReferenceCode ────────────────────────────────────────────

    @Test
    void getPropertyByReferenceCode_whenFound_shouldReturnResponse() {
        when(propertyRepository.findByReferenceCode("PROP-1001"))
                .thenReturn(Optional.of(buildProperty("PROP-1001")));

        Optional<PropertyResponse> result = service.getPropertyByReferenceCode("PROP-1001");

        assertThat(result).isPresent();
        assertThat(result.get().referenceCode()).isEqualTo("PROP-1001");
    }

    @Test
    void getPropertyByReferenceCode_whenNotFound_shouldReturnEmpty() {
        when(propertyRepository.findByReferenceCode("PROP-9999"))
                .thenReturn(Optional.empty());

        Optional<PropertyResponse> result = service.getPropertyByReferenceCode("PROP-9999");

        assertThat(result).isEmpty();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Property buildProperty(String referenceCode) {
        return Property.builder()
                .id(new PropertyId(1L))
                .referenceCode(referenceCode)
                .title("Test Property")
                .propertyType(PropertyType.APARTMENT)
                .listingType(ListingType.SALE)
                .status(PropertyStatus.AVAILABLE)
                .price(new BigDecimal("185000"))
                .currency("EUR")
                .bedrooms(2)
                .bathrooms(1)
                .city("Tirana")
                .country("Albania")
                .images(List.of())
                .amenities(List.of())
                .nearbyPlaces(List.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
