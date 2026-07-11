package com.ai.assistant.real.estate.chat.interfaces.rest;

import com.ai.assistant.real.estate.chat.application.dto.ChatRequest;
import com.ai.assistant.real.estate.chat.application.dto.ChatResponse;
import org.springframework.ai.chat.client.ChatClient;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatClient assistant;

    public ChatController(ChatClient assistant) {
        this.assistant = assistant;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(
            @RequestBody ChatRequest request,
            @RequestHeader(value = "X-Conversation-Id", defaultValue = "default") String conversationId) {

        String content = assistant.prompt(request.message())
                .advisors(a -> a.param("conversationId", conversationId))
                .call()
                .content();

        return ResponseEntity.ok(new ChatResponse(content));
    }
}
