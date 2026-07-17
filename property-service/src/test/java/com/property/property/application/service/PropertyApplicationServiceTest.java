package com.property.property.application.service;

import com.property.property.application.dto.PropertyResponse;
import com.property.property.domain.model.PropertySearchParams;
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
    void searchProperties_withEmptyParams_shouldPassCriteriaWithAllNulls() {
        when(propertyRepository.search(any())).thenReturn(List.of());

        service.searchProperties(emptyParams());

        var captor = ArgumentCaptor.forClass(PropertySearchCriteria.class);
        verify(propertyRepository).search(captor.capture());

        PropertySearchCriteria c = captor.getValue();
        assertThat(c.title()).isNull();
        assertThat(c.description()).isNull();
        assertThat(c.propertyType()).isNull();
        assertThat(c.listingType()).isNull();
        assertThat(c.status()).isNull();
        assertThat(c.city()).isNull();
        assertThat(c.country()).isNull();
        assertThat(c.minPrice()).isNull();
        assertThat(c.maxPrice()).isNull();
        assertThat(c.minBedrooms()).isNull();
        assertThat(c.maxBedrooms()).isNull();
        assertThat(c.minBathrooms()).isNull();
        assertThat(c.maxBathrooms()).isNull();
        assertThat(c.minArea()).isNull();
        assertThat(c.maxArea()).isNull();
        assertThat(c.minFloor()).isNull();
        assertThat(c.maxFloor()).isNull();
        assertThat(c.minYearBuilt()).isNull();
        assertThat(c.maxYearBuilt()).isNull();
    }

    @Test
    void searchProperties_shouldMapAllParamsToCriteria() {
        when(propertyRepository.search(any())).thenReturn(List.of());

        var params = new PropertySearchParams(
                "Luxury", "spacious", "APARTMENT", "SALE", "AVAILABLE",
                "Tirana", "Albania",
                new BigDecimal("100000"), new BigDecimal("200000"),
                2, 4,
                1, 2,
                new BigDecimal("60"), new BigDecimal("150"),
                1, 5,
                2010, 2023
        );

        service.searchProperties(params);

        var captor = ArgumentCaptor.forClass(PropertySearchCriteria.class);
        verify(propertyRepository).search(captor.capture());

        PropertySearchCriteria c = captor.getValue();
        assertThat(c.title()).isEqualTo("Luxury");
        assertThat(c.description()).isEqualTo("spacious");
        assertThat(c.propertyType()).isEqualTo(PropertyType.APARTMENT);
        assertThat(c.listingType()).isEqualTo(ListingType.SALE);
        assertThat(c.status()).isEqualTo(PropertyStatus.AVAILABLE);
        assertThat(c.city()).isEqualTo("Tirana");
        assertThat(c.country()).isEqualTo("Albania");
        assertThat(c.minPrice()).isEqualTo(new BigDecimal("100000"));
        assertThat(c.maxPrice()).isEqualTo(new BigDecimal("200000"));
        assertThat(c.minBedrooms()).isEqualTo(2);
        assertThat(c.maxBedrooms()).isEqualTo(4);
        assertThat(c.minBathrooms()).isEqualTo(1);
        assertThat(c.maxBathrooms()).isEqualTo(2);
        assertThat(c.minArea()).isEqualTo(new BigDecimal("60"));
        assertThat(c.maxArea()).isEqualTo(new BigDecimal("150"));
        assertThat(c.minFloor()).isEqualTo(1);
        assertThat(c.maxFloor()).isEqualTo(5);
        assertThat(c.minYearBuilt()).isEqualTo(2010);
        assertThat(c.maxYearBuilt()).isEqualTo(2023);
    }

    @Test
    void searchProperties_withLowercaseEnums_shouldParseCorrectly() {
        when(propertyRepository.search(any())).thenReturn(List.of());

        service.searchProperties(paramsWithTypes("apartment", "rent", "available"));

        var captor = ArgumentCaptor.forClass(PropertySearchCriteria.class);
        verify(propertyRepository).search(captor.capture());

        assertThat(captor.getValue().propertyType()).isEqualTo(PropertyType.APARTMENT);
        assertThat(captor.getValue().listingType()).isEqualTo(ListingType.RENT);
        assertThat(captor.getValue().status()).isEqualTo(PropertyStatus.AVAILABLE);
    }

    @Test
    void searchProperties_withInvalidPropertyType_shouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> service.searchProperties(paramsWithTypes("INVALID_TYPE", null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void searchProperties_withInvalidListingType_shouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> service.searchProperties(paramsWithTypes(null, "INVALID_TYPE", null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void searchProperties_withInvalidStatus_shouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> service.searchProperties(paramsWithTypes(null, null, "INVALID_STATUS")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void searchProperties_shouldReturnMappedResponses() {
        when(propertyRepository.search(any())).thenReturn(List.of(buildProperty("PROP-1001")));

        List<PropertyResponse> result = service.searchProperties(emptyParams());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).referenceCode()).isEqualTo("PROP-1001");
        assertThat(result.get(0).city()).isEqualTo("Tirana");
    }

    @Test
    void searchProperties_whenRepositoryReturnsEmpty_shouldReturnEmptyList() {
        when(propertyRepository.search(any())).thenReturn(List.of());

        List<PropertyResponse> result = service.searchProperties(emptyParams());

        assertThat(result).isEmpty();
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

    private PropertySearchParams emptyParams() {
        return new PropertySearchParams(
                null, null, null, null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null
        );
    }

    private PropertySearchParams paramsWithTypes(String propertyType, String listingType, String status) {
        return new PropertySearchParams(
                null, null, propertyType, listingType, status,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null
        );
    }

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
