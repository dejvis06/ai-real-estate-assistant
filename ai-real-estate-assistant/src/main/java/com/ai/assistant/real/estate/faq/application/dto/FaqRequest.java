package com.ai.assistant.real.estate.faq.application.dto;

import com.ai.assistant.real.estate.faq.domain.model.FaqCategory;

import java.util.List;

public record FaqRequest(
        String question,
        String answer,
        FaqCategory category,
        List<String> keywords,
        Boolean active
) {}
