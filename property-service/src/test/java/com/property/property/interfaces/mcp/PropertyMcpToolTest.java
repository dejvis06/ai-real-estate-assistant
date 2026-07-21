package com.property.property.interfaces.mcp;

import com.property.property.application.dto.PropertyResponse;
import com.property.property.application.service.PropertyApplicationService;
import com.property.property.domain.model.PropertySearchParams;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertyMcpToolTest {

    @Mock
    PropertyApplicationService propertyApplicationService;

    @InjectMocks
    PropertyMcpTool tool;

    // ── searchProperties ─────────────────────────────────────────────────────

    @Test
    void searchProperties_shouldDelegateAllParamsToApplicationService() {
        when(propertyApplicationService.searchProperties(any())).thenReturn(List.of(buildResponse("PROP-1001")));

        tool.searchProperties(
                "Luxury", "spacious", "APARTMENT", "SALE", "AVAILABLE",
                "Tirana", "Albania",
                new BigDecimal("100000"), new BigDecimal("200000"),
                2, 4,
                1, 2,
                new BigDecimal("60"), new BigDecimal("150"),
                1, 5,
                2010, 2023
        );

        var captor = ArgumentCaptor.forClass(PropertySearchParams.class);
        verify(propertyApplicationService).searchProperties(captor.capture());

        PropertySearchParams params = captor.getValue();
        assertThat(params.title()).isEqualTo("Luxury");
        assertThat(params.description()).isEqualTo("spacious");
        assertThat(params.propertyType()).isEqualTo("APARTMENT");
        assertThat(params.listingType()).isEqualTo("SALE");
        assertThat(params.status()).isEqualTo("AVAILABLE");
        assertThat(params.city()).isEqualTo("Tirana");
        assertThat(params.country()).isEqualTo("Albania");
        assertThat(params.minPrice()).isEqualTo(new BigDecimal("100000"));
        assertThat(params.maxPrice()).isEqualTo(new BigDecimal("200000"));
        assertThat(params.minBedrooms()).isEqualTo(2);
        assertThat(params.maxBedrooms()).isEqualTo(4);
        assertThat(params.minBathrooms()).isEqualTo(1);
        assertThat(params.maxBathrooms()).isEqualTo(2);
        assertThat(params.minArea()).isEqualTo(new BigDecimal("60"));
        assertThat(params.maxArea()).isEqualTo(new BigDecimal("150"));
        assertThat(params.minFloor()).isEqualTo(1);
        assertThat(params.maxFloor()).isEqualTo(5);
        assertThat(params.minYearBuilt()).isEqualTo(2010);
        assertThat(params.maxYearBuilt()).isEqualTo(2023);
    }

    @Test
    void searchProperties_withAllNulls_shouldPassEmptyParamsToService() {
        when(propertyApplicationService.searchProperties(any())).thenReturn(List.of());

        tool.searchProperties(
                null, null, null, null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null
        );

        var captor = ArgumentCaptor.forClass(PropertySearchParams.class);
        verify(propertyApplicationService).searchProperties(captor.capture());

        PropertySearchParams params = captor.getValue();
        assertThat(params.title()).isNull();
        assertThat(params.city()).isNull();
        assertThat(params.minPrice()).isNull();
        assertThat(params.minBedrooms()).isNull();
    }

    @Test
    void searchProperties_shouldReturnAllResultsFromService() {
        when(propertyApplicationService.searchProperties(any()))
                .thenReturn(List.of(buildResponse("PROP-1001"), buildResponse("PROP-1003")));

        List<PropertyResponse> result = tool.searchProperties(
                null, null, null, null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null
        );

        assertThat(result).hasSize(2);
        assertThat(result).extracting(PropertyResponse::referenceCode)
                .containsExactly("PROP-1001", "PROP-1003");
    }

    // ── getPropertyByReferenceCode ────────────────────────────────────────────

    @Test
    void getPropertyByReferenceCode_whenFound_shouldReturnOptionalWithResult() {
        when(propertyApplicationService.getPropertyByReferenceCode("PROP-1001"))
                .thenReturn(Optional.of(buildResponse("PROP-1001")));

        Optional<PropertyResponse> result = tool.getPropertyByReferenceCode("PROP-1001");

        assertThat(result).isPresent();
        assertThat(result.get().referenceCode()).isEqualTo("PROP-1001");
    }

    @Test
    void getPropertyByReferenceCode_whenNotFound_shouldReturnEmpty() {
        when(propertyApplicationService.getPropertyByReferenceCode("PROP-9999"))
                .thenReturn(Optional.empty());

        Optional<PropertyResponse> result = tool.getPropertyByReferenceCode("PROP-9999");

        assertThat(result).isEmpty();
    }

    @Test
    void getPropertyByReferenceCode_shouldDelegateReferenceCode() {
        when(propertyApplicationService.getPropertyByReferenceCode("PROP-1002"))
                .thenReturn(Optional.empty());

        tool.getPropertyByReferenceCode("PROP-1002");

        verify(propertyApplicationService).getPropertyByReferenceCode("PROP-1002");
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private PropertyResponse buildResponse(String referenceCode) {
        return new PropertyResponse(
                1L, referenceCode, "Title", "Description",
                "APARTMENT", "SALE", "AVAILABLE",
                new BigDecimal("185000"), "EUR",
                2, 1, new BigDecimal("75.5"),
                4, 8, 2019,
                "Street 1", "Tirana", "Albania",
                new BigDecimal("41.32"), new BigDecimal("19.81"),
                1L, List.of(), List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now()
        );
    }
}
