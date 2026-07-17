package com.property.property.interfaces.mcp;

import com.property.property.application.dto.PropertyResponse;
import com.property.property.domain.model.PropertySearchParams;
import com.property.property.application.service.PropertyApplicationService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

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
            All parameters are optional — omit any that are not relevant to the customer's request.
            Supported filters:
            - title: keyword match on the listing title (optional)
            - description: keyword match on the listing description (optional)
            - propertyType: APARTMENT, HOUSE, VILLA, LAND, OFFICE (optional)
            - listingType: SALE, RENT (optional)
            - status: AVAILABLE, RESERVED, SOLD, RENTED — defaults to AVAILABLE (optional)
            - city, country: partial text match on location fields (optional)
            - minPrice / maxPrice: price range in the property's currency (optional)
            - minBedrooms / maxBedrooms: bedroom count range (optional)
            - minBathrooms / maxBathrooms: bathroom count range (optional)
            - minArea / maxArea: total area range in square metres (optional)
            - minFloor / maxFloor: floor number range (optional)
            - minYearBuilt / maxYearBuilt: construction year range (optional)
            """)
    public List<PropertyResponse> searchProperties(
            @ToolParam(description = "Search filters for available properties. All fields are optional.") PropertySearchParams params) {

        return propertyApplicationService.searchProperties(params);
    }

    @Tool(description = "Get full details of a specific property by its unique reference code (e.g. PROP-1001).")
    public Optional<PropertyResponse> getPropertyByReferenceCode(
            @ToolParam(description = "The property reference code, e.g. PROP-1001") String referenceCode) {

        return propertyApplicationService.getPropertyByReferenceCode(referenceCode);
    }
}
