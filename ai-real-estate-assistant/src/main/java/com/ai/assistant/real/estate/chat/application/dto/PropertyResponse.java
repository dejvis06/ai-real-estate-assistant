package com.ai.assistant.real.estate.chat.application.dto;

import java.util.List;

public record PropertyResponse(
        String type,
        String message,
        List<PropertyCardDto> properties
) {
    public static PropertyResponse text(String message) {
        return new PropertyResponse("text", message, List.of());
    }

    public static PropertyResponse propertyList(String message, List<PropertyCardDto> properties) {
        return new PropertyResponse("property_list", message, properties);
    }
}
