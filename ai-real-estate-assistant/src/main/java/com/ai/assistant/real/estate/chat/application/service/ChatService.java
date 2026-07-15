package com.ai.assistant.real.estate.chat.application.service;

import com.ai.assistant.real.estate.chat.application.dto.ChatResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Coordinates user requests by routing them to the appropriate specialized agent.
 *
 * <pre>
 * ChatController
 *       │
 *       ▼
 * ChatService  (agent resolved from request AgentType)
 *       │
 *       ├── FAQ ─────────────────► FaqAgent         (RAG / Vector Store)
 *       ├── PROPERTY ────────────► PropertyAgent    (Property MCP tools)
 *       └── RESERVATION ─────────► ReservationAgent (Reservation MCP tools)
 * </pre>
 */
@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatClient faqAgent;
    private final ChatClient propertyAgent;
    private final ChatClient reservationAgent;
    private final ObjectMapper objectMapper;

    public ChatService(@Qualifier("faqAgent") ChatClient faqAgent,
                       @Qualifier("propertyAgent") ChatClient propertyAgent,
                       @Qualifier("reservationAgent") ChatClient reservationAgent,
                       ObjectMapper objectMapper) {
        this.faqAgent = faqAgent;
        this.propertyAgent = propertyAgent;
        this.reservationAgent = reservationAgent;
        this.objectMapper = objectMapper;
    }

    public ChatResponse chat(String message, AgentType agentType, String conversationId) {
        log.info("Routing message to agent [{}] for conversationId [{}]", agentType, conversationId);

        ChatClient agent = resolveAgent(agentType);

        String raw = agent.prompt(message)
                .advisors(a -> a.param("chat_memory_conversation_id", conversationId))
                .call()
                .content();

        return parse(raw);
    }

    private ChatClient resolveAgent(AgentType agentType) {
        if (agentType == null) {
            return faqAgent;
        }
        return switch (agentType) {
            case PROPERTY    -> propertyAgent;
            case RESERVATION -> reservationAgent;
            case FAQ         -> faqAgent;
        };
    }

    private ChatResponse parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return ChatResponse.text("I'm sorry, I couldn't generate a response. Please try again.");
        }

        try {
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
