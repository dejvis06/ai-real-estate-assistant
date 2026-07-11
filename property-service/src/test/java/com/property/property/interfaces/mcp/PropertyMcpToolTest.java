package com.property.property.interfaces.mcp;

import com.property.property.application.dto.PropertyResponse;
import com.property.property.application.service.PropertyApplicationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
    void searchProperties_shouldDelegateToApplicationService() {
        when(propertyApplicationService.searchProperties(
                "Tirana", "APARTMENT", "SALE",
                new BigDecimal("100000"), new BigDecimal("200000"), 2))
                .thenReturn(List.of(buildResponse("PROP-1001")));

        List<PropertyResponse> result = tool.searchProperties(
                "Tirana", "APARTMENT", "SALE",
                new BigDecimal("100000"), new BigDecimal("200000"), 2);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).referenceCode()).isEqualTo("PROP-1001");
        verify(propertyApplicationService).searchProperties(
                "Tirana", "APARTMENT", "SALE",
                new BigDecimal("100000"), new BigDecimal("200000"), 2);
    }

    @Test
    void searchProperties_withNullParameters_shouldPassNullsThroughToService() {
        when(propertyApplicationService.searchProperties(null, null, null, null, null, null))
                .thenReturn(List.of());

        List<PropertyResponse> result = tool.searchProperties(null, null, null, null, null, null);

        assertThat(result).isEmpty();
        verify(propertyApplicationService).searchProperties(null, null, null, null, null, null);
    }

    @Test
    void searchProperties_shouldReturnAllResultsFromService() {
        when(propertyApplicationService.searchProperties(null, null, "SALE", null, null, null))
                .thenReturn(List.of(buildResponse("PROP-1001"), buildResponse("PROP-1003")));

        List<PropertyResponse> result = tool.searchProperties(null, null, "SALE", null, null, null);

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
