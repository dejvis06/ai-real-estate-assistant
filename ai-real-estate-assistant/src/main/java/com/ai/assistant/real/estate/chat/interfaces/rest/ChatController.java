package com.ai.assistant.real.estate.chat.interfaces.rest;

import com.ai.assistant.real.estate.chat.application.dto.ChatRequest;
import com.ai.assistant.real.estate.chat.application.dto.ChatResponse;
import com.ai.assistant.real.estate.chat.application.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(
            @RequestBody ChatRequest request,
            @RequestHeader(value = "X-Conversation-Id", defaultValue = "default") String conversationId) {

        return ResponseEntity.ok(chatService.chat(request.message(), request.agentType(), conversationId));
    }
}
