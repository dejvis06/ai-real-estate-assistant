package com.ai.assistant.real.estate.faq.domain.model;

import java.util.Objects;

public record FaqId(Long value) {

    public FaqId {
        Objects.requireNonNull(value, "FaqId value must not be null");
    }
}
