package com.ai.assistant.real.estate.faq.application.service;

public class FaqNotFoundException extends RuntimeException {

    public FaqNotFoundException(Long id) {
        super("FAQ not found with id: " + id);
    }
}
