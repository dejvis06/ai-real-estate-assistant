package com.ai.assistant.real.estate.chat.application.service;

import com.ai.assistant.real.estate.chat.application.dto.ChatResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatClient assistant;
    private final ObjectMapper objectMapper;

    public ChatService(ChatClient assistant, ObjectMapper objectMapper) {
        this.assistant = assistant;
        this.objectMapper = objectMapper;
    }

    public ChatResponse chat(String message, String conversationId) {
        String raw = assistant.prompt(message)
                .advisors(a -> a.param("chat_memory_conversation_id", conversationId))
                .call()
                .content();

        return parse(raw);
    }

    private ChatResponse parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return ChatResponse.text("I'm sorry, I couldn't generate a response. Please try again.");
        }

        try {
            // Strip markdown code fences if the model wraps JSON in ```json ... ```
            String json = raw.strip();
            if (json.startsWith("```")) {
                json = json.replaceAll("(?s)^```[a-z]*\\n?", "").replaceAll("```$", "").strip();
            }
            return objectMapper.readValue(json, ChatResponse.class);
        } catch (Exception e) {
            log.warn("AI response was not valid JSON, falling back to plain text. Raw: {}", raw);
            return ChatResponse.text(raw);
        }
    }
}
