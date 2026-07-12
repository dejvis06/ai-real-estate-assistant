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

            CRITICAL — Response format:
            You MUST always respond with a single valid JSON object. Never output any text outside the JSON.

            For conversational answers and FAQs:
            {"type":"text","message":"your answer here","properties":[]}

            For one or more property results:
            {
              "type":"property_list",
              "message":"brief intro sentence (e.g. Here are the properties I found for you)",
              "properties":[
                {
                  "referenceCode":"PROP-1001",
                  "title":"Modern Apartment",
                  "description":"Short description of the property.",
                  "propertyType":"Apartment",
                  "listingType":"Sale",
                  "status":"Available",
                  "price":185000.0,
                  "currency":"EUR",
                  "bedrooms":2,
                  "bathrooms":1,
                  "area":85.0,
                  "city":"Tirana Center",
                  "country":"Albania",
                  "address":"Street address here",
                  "imageUrls":["https://images.example.com/prop-1001/main.jpg"],
                  "amenities":["Pool","Gym"]
                }
              ]
            }

            Rules:
            - Always use the available tools to search property listings when the customer asks about properties.
            - Never embed property details inside the message text; always use the properties array.
            - If the customer's criteria are vague, ask clarifying questions using the text type.
            - Keep message values concise and friendly.
            - Do not wrap the JSON in markdown code fences.
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
