package com.ai.assistant.real.estate.chat.application.service;

import com.ai.assistant.real.estate.chat.application.dto.PropertyResponse;
import com.ai.assistant.real.estate.chat.application.dto.TextResponse;
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
 *       ├── FAQ ─────────────────► FaqAgent         (RAG / Vector Store)  → TextResponse
 *       ├── PROPERTY ────────────► PropertyAgent    (Property MCP tools)  → PropertyResponse
 *       └── RESERVATION ─────────► ReservationAgent (Reservation MCP tools) → TextResponse
 * </pre>
 *
 * <p>Each agent has its own {@code ChatMemory} bean. To further ensure that
 * conversation histories never bleed across agents in the shared
 * {@code SPRING_AI_CHAT_MEMORY} table, the conversation ID passed to each
 * agent is prefixed with the agent name:
 * <pre>
 *   faq:{conversationId}
 *   property:{conversationId}
 *   reservation:{conversationId}
 * </pre>
 *
 * <p>Response handling per agent:
 * <ul>
 *   <li>PROPERTY — agent replies in JSON; raw response is parsed into {@link PropertyResponse}.</li>
 *   <li>FAQ / RESERVATION — agent replies in plain natural language; raw string is
 *       wrapped into {@link TextResponse} without any JSON parsing.</li>
 * </ul>
 *
 * <p>Reservation context: when {@code agentType} is {@code RESERVATION} and a
 * {@code reservationId} is provided, it is appended to the user message so the
 * agent can pass it directly to the MCP tool without asking the user for it.
 */
@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private static final String FAQ_PREFIX         = "faq:";
    private static final String PROPERTY_PREFIX    = "property:";
    private static final String RESERVATION_PREFIX = "reservation:";

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

    public Object chat(String message, AgentType agentType, Long reservationId, String conversationId) {
        AgentType resolved = agentType != null ? agentType : AgentType.FAQ;

        String scopedConversationId = prefix(resolved) + conversationId;

        String prompt = buildPrompt(message, resolved, reservationId);

        log.info("Routing message to agent [{}] for conversationId [{}]", resolved, scopedConversationId);

        String raw = resolveAgent(resolved).prompt(prompt)
                .advisors(a -> a.param("chat_memory_conversation_id", scopedConversationId))
                .call()
                .content();

        return resolved == AgentType.PROPERTY ? parsePropertyJson(raw) : wrapText(raw);
    }

    /**
     * Builds the prompt sent to the agent.
     *
     * <p>For the Reservation agent, the reservation ID is appended as a system
     * context line so the agent can supply it to the MCP tool without prompting
     * the user. The user-visible message is left unchanged.
     */
    private String buildPrompt(String message, AgentType agentType, Long reservationId) {
        if (agentType == AgentType.RESERVATION && reservationId != null) {
            return message + "\n[Reservation ID: " + reservationId + "]";
        }
        return message;
    }

    private ChatClient resolveAgent(AgentType agentType) {
        return switch (agentType) {
            case PROPERTY    -> propertyAgent;
            case RESERVATION -> reservationAgent;
            case FAQ         -> faqAgent;
        };
    }

    private String prefix(AgentType agentType) {
        return switch (agentType) {
            case PROPERTY    -> PROPERTY_PREFIX;
            case RESERVATION -> RESERVATION_PREFIX;
            case FAQ         -> FAQ_PREFIX;
        };
    }

    private TextResponse wrapText(String raw) {
        if (raw == null || raw.isBlank()) {
            return TextResponse.of("I'm sorry, I couldn't generate a response. Please try again.");
        }
        return TextResponse.of(raw.strip());
    }

    private PropertyResponse parsePropertyJson(String raw) {
        if (raw == null || raw.isBlank()) {
            return PropertyResponse.text("I'm sorry, I couldn't generate a response. Please try again.");
        }
        try {
            String json = raw.strip();
            if (json.startsWith("```")) {
                json = json.replaceAll("(?s)^```[a-z]*\\n?", "").replaceAll("```$", "").strip();
            }
            return objectMapper.readValue(json, PropertyResponse.class);
        } catch (Exception e) {
            log.warn("Property agent response was not valid JSON, falling back to plain text. Raw: {}", raw);
            return PropertyResponse.text(raw);
        }
    }
}
