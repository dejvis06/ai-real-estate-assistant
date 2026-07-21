package com.ai.assistant.real.estate.chat.application.dto;

public record TextResponse(String message) {

    public static TextResponse of(String message) {
        return new TextResponse(message);
    }
}
