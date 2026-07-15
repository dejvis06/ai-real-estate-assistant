package com.ai.assistant.real.estate.chat.application.dto;

import com.ai.assistant.real.estate.chat.application.service.AgentType;

public record ChatRequest(String message, AgentType agentType) {}
