package com.ai.assistant.real.estate.chat.application.dto;

import java.util.List;

public record ChatResponse(
        String type,
        String message,
        List<PropertyCardDto> properties
) {
    public static ChatResponse text(String message) {
        return new ChatResponse("text", message, List.of());
    }

    public static ChatResponse propertyList(String message, List<PropertyCardDto> properties) {
        return new ChatResponse("property_list", message, properties);
    }
}
