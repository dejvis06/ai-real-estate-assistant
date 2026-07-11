package com.property.property.interfaces.mcp;

import com.property.property.application.dto.PropertyResponse;
import com.property.property.application.service.PropertyApplicationService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
public class PropertyMcpTool {

    private final PropertyApplicationService propertyApplicationService;

    public PropertyMcpTool(PropertyApplicationService propertyApplicationService) {
        this.propertyApplicationService = propertyApplicationService;
    }

    @Tool(description = "Search for available properties based on optional filters. Returns a list of matching properties with full details including images, amenities, and nearby places.")
    public List<PropertyResponse> searchProperties(
            @ToolParam(description = "City where the property is located (optional, partial match supported)") String city,
            @ToolParam(description = "Property type: APARTMENT, HOUSE, VILLA, LAND, or OFFICE (optional)") String propertyType,
            @ToolParam(description = "Listing type: SALE or RENT (optional)") String listingType,
            @ToolParam(description = "Minimum listing price in the property's currency (optional)") BigDecimal minPrice,
            @ToolParam(description = "Maximum listing price in the property's currency (optional)") BigDecimal maxPrice,
            @ToolParam(description = "Minimum number of bedrooms required (optional)") Integer minBedrooms) {

        return propertyApplicationService.searchProperties(
                city, propertyType, listingType, minPrice, maxPrice, minBedrooms);
    }

    @Tool(description = "Get full details of a specific property by its unique reference code (e.g. PROP-1001).")
    public Optional<PropertyResponse> getPropertyByReferenceCode(
            @ToolParam(description = "The property reference code, e.g. PROP-1001") String referenceCode) {

        return propertyApplicationService.getPropertyByReferenceCode(referenceCode);
    }
}
