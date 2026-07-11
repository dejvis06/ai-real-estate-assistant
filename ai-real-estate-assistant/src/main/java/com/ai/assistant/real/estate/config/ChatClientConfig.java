package com.ai.assistant.real.estate.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.jdbc.PostgresChatMemoryRepositoryDialect;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
class ChatClientConfig {

    private static final String SYSTEM_PROMPT = """
            You are a professional real estate assistant. Your role is to help customers with:
            - Finding properties for sale or rent based on their preferences
            - Answering frequently asked questions about buying, selling, financing, and company policies
            - Providing detailed information about specific properties

            Only answer questions related to real estate. For unrelated topics, politely explain
            that you specialize in real estate assistance only.

            When a customer asks about properties, use the available tools to search listings
            based on their criteria (city, property type, budget, bedrooms, etc.).
            Always present property results clearly, highlighting key details like price, location,
            and features. If a customer's criteria are vague, ask clarifying questions.
            """;

    @Bean
    ChatMemory chatMemory(JdbcTemplate jdbcTemplate) {
        var chatMemoryRepository = JdbcChatMemoryRepository.builder()
                .jdbcTemplate(jdbcTemplate)
                .dialect(new PostgresChatMemoryRepositoryDialect())
                .build();

        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(30)
                .build();
    }

    @Bean
    ChatClient assistant(OpenAiChatModel chatModel,
                         ChatMemory chatMemory,
                         VectorStore vectorStore,
                         ToolCallbackProvider mcpToolCallbackProvider) {

        var faqAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder()
                        .similarityThreshold(0.8d)
                        .topK(6)
                        .build())
                .build();

        return ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        faqAdvisor
                )
                .defaultTools(mcpToolCallbackProvider)
                .build();
    }
}
