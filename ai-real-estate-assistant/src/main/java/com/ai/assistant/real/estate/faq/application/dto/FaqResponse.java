package com.ai.assistant.real.estate.faq.application.dto;

import com.ai.assistant.real.estate.faq.domain.model.Faq;

import java.time.LocalDateTime;
import java.util.List;

public record FaqResponse(
        Long id,
        String question,
        String answer,
        String category,
        List<String> keywords,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static FaqResponse from(Faq faq) {
        return new FaqResponse(
                faq.getId().value(),
                faq.getQuestion(),
                faq.getAnswer(),
                faq.getCategory().name(),
                faq.getKeywords(),
                faq.isActive(),
                faq.getCreatedAt(),
                faq.getUpdatedAt()
        );
    }
}
