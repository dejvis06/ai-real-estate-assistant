package com.property.property.interfaces.mcp;

import com.property.property.application.dto.PropertyResponse;
import com.property.property.application.service.PropertyApplicationService;
import com.property.property.domain.model.PropertySearchParams;
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

    @Tool(description = """
            Search for available properties based on optional filters.
            Returns a list of matching properties with full details including images, amenities, and nearby places.
            All filters are optional — omit any that are not relevant to the customer's request.
            """)
    public List<PropertyResponse> searchProperties(
            @ToolParam(required = false, description = "Keyword match on the listing title") String title,
            @ToolParam(required = false, description = "Keyword match on the listing description") String description,
            @ToolParam(required = false, description = "Property type: APARTMENT, HOUSE, VILLA, LAND, OFFICE") String propertyType,
            @ToolParam(required = false, description = "Listing type: SALE or RENT") String listingType,
            @ToolParam(required = false, description = "Status: AVAILABLE, RESERVED, SOLD, RENTED — defaults to AVAILABLE") String status,
            @ToolParam(required = false, description = "Partial match on the city name") String city,
            @ToolParam(required = false, description = "Partial match on the country name") String country,
            @ToolParam(required = false, description = "Minimum price in the property's currency") BigDecimal minPrice,
            @ToolParam(required = false, description = "Maximum price in the property's currency") BigDecimal maxPrice,
            @ToolParam(required = false, description = "Minimum number of bedrooms") Integer minBedrooms,
            @ToolParam(required = false, description = "Maximum number of bedrooms") Integer maxBedrooms,
            @ToolParam(required = false, description = "Minimum number of bathrooms") Integer minBathrooms,
            @ToolParam(required = false, description = "Maximum number of bathrooms") Integer maxBathrooms,
            @ToolParam(required = false, description = "Minimum area in square metres") BigDecimal minArea,
            @ToolParam(required = false, description = "Maximum area in square metres") BigDecimal maxArea,
            @ToolParam(required = false, description = "Minimum floor number") Integer minFloor,
            @ToolParam(required = false, description = "Maximum floor number") Integer maxFloor,
            @ToolParam(required = false, description = "Minimum construction year") Integer minYearBuilt,
            @ToolParam(required = false, description = "Maximum construction year") Integer maxYearBuilt) {

        return propertyApplicationService.searchProperties(new PropertySearchParams(
                title, description, propertyType, listingType, status,
                city, country,
                minPrice, maxPrice,
                minBedrooms, maxBedrooms,
                minBathrooms, maxBathrooms,
                minArea, maxArea,
                minFloor, maxFloor,
                minYearBuilt, maxYearBuilt
        ));
    }

    @Tool(description = "Get full details of a specific property by its unique reference code (e.g. PROP-1001).")
    public Optional<PropertyResponse> getPropertyByReferenceCode(
            @ToolParam(description = "The property reference code, e.g. PROP-1001") String referenceCode) {

        return propertyApplicationService.getPropertyByReferenceCode(referenceCode);
    }
}
