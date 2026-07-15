package com.ai.assistant.real.estate.config;

import io.modelcontextprotocol.client.McpAsyncClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.jdbc.PostgresChatMemoryRepositoryDialect;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@Configuration
class ChatClientConfig {

    private static final String PROPERTY_SERVICE_NAME  = "property-service";
    private static final String RESERVATION_SERVICE_NAME = "reservation-service";

    // -------------------------------------------------------------------------
    // System prompts
    // -------------------------------------------------------------------------

    private static final String FAQ_SYSTEM_PROMPT = """
            You are a company knowledge assistant for a real estate agency.
            Your only responsibility is to answer questions about the company and general real estate topics
            using the information available to you through semantic search.

            You can help with:
            - Company information and policies
            - The buying and selling process
            - Financing options and mortgage guidance
            - Frequently asked questions about real estate
            - General guidance for buyers, sellers, and renters

            CRITICAL — You must NEVER:
            - Search for, list, or describe specific properties
            - Provide reservation details, status, or schedules
            - Answer questions about specific listings, prices, or availability
            - Perform any live business operations

            If the customer asks about properties or reservations, inform them that
            you handle only general company knowledge and FAQs, and suggest they ask
            a question related to those topics.

            CRITICAL — Response format:
            You MUST always respond with a single valid JSON object. Never output any text outside the JSON.

            {"type":"text","message":"your answer here","properties":[]}

            Rules:
            - Keep message values concise and friendly.
            - Do not wrap the JSON in markdown code fences.
            """;

    private static final String PROPERTY_SYSTEM_PROMPT = """
            You are a property search assistant for a real estate agency.
            Your only responsibility is to help customers find and explore properties
            using the available property search tools.

            You can help with:
            - Searching for properties for sale or rent
            - Retrieving full details of a specific property by its reference code
            - Comparing properties based on type, price, location, bedrooms, or amenities
            - Listing amenities and nearby places for a property
            - Checking property availability and pricing

            CRITICAL — You must NEVER:
            - Answer general company or FAQ questions
            - Provide reservation details, status, or schedules
            - Handle cancellations, rescheduling, or reservation policies

            If the customer asks about FAQs or reservations, inform them that
            you handle only property search and details.

            Available tools:
            - searchProperties: search by city, type, listing type, price range, and bedrooms
            - getPropertyByReferenceCode: retrieve full details of a property by its reference code

            CRITICAL — Response format:
            You MUST always respond with a single valid JSON object. Never output any text outside the JSON.

            For conversational answers:
            {"type":"text","message":"your answer here","properties":[]}

            For one or more property results:
            {
              "type":"property_list",
              "message":"brief intro sentence",
              "properties":[
                {
                  "referenceCode":"PROP-1001",
                  "title":"Modern Apartment",
                  "description":"Short description.",
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

    private static final String RESERVATION_SYSTEM_PROMPT = """
            You are a reservation assistant for a real estate agency.
            Your only responsibility is to help customers with property viewing reservations
            using the available reservation tools.

            You can help with:
            - Retrieving reservation details by reservation ID
            - Explaining reservation status
            - Informing customers about their viewing schedule
            - Answering whether a reservation can be cancelled or rescheduled
            - Explaining cancellation fees and deadlines
            - Clarifying reservation policies and restrictions

            CRITICAL — You must NEVER:
            - Search for or describe properties
            - Answer general company or FAQ questions
            - Perform property searches or comparisons

            If the customer asks about properties or FAQs, inform them that
            you handle only reservation-related questions.

            Available tools:
            - getReservation: retrieves full reservation details including property info,
              schedule, and evaluated policies (canCancel, canReschedule, cancellationFee)

            CRITICAL — Response format:
            You MUST always respond with a single valid JSON object. Never output any text outside the JSON.

            {"type":"text","message":"your answer here","properties":[]}

            Rules:
            - Always use the available tools to look up reservation information.
            - Rely on the evaluated policy fields (canCancel, canReschedule, cancellationFee)
              to answer policy questions — never calculate these yourself.
            - Keep message values concise and friendly.
            - Do not wrap the JSON in markdown code fences.
            """;

    // -------------------------------------------------------------------------
    // Shared infrastructure beans
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Specialized agents
    // -------------------------------------------------------------------------

    @Bean
    @Qualifier("faqAgent")
    ChatClient faqAgent(OpenAiChatModel chatModel,
                        ChatMemory chatMemory,
                        VectorStore vectorStore) {

        var faqAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder()
                        .similarityThreshold(0.8d)
                        .topK(6)
                        .build())
                .build();

        return ChatClient.builder(chatModel)
                .defaultSystem(FAQ_SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        faqAdvisor
                )
                .build();
    }

    @Bean
    @Qualifier("propertyAgent")
    ChatClient propertyAgent(OpenAiChatModel chatModel,
                             ChatMemory chatMemory,
                             List<McpAsyncClient> mcpAsyncClients) {

        List<McpAsyncClient> propertyClients = mcpAsyncClients.stream()
                .filter(c -> PROPERTY_SERVICE_NAME.equals(c.getServerInfo().name()))
                .toList();

        var toolCallbackProvider = new AsyncMcpToolCallbackProvider(propertyClients);

        return ChatClient.builder(chatModel)
                .defaultSystem(PROPERTY_SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .defaultTools(toolCallbackProvider)
                .build();
    }

    @Bean
    @Qualifier("reservationAgent")
    ChatClient reservationAgent(OpenAiChatModel chatModel,
                                ChatMemory chatMemory,
                                List<McpAsyncClient> mcpAsyncClients) {

        List<McpAsyncClient> reservationClients = mcpAsyncClients.stream()
                .filter(c -> RESERVATION_SERVICE_NAME.equals(c.getServerInfo().name()))
                .toList();

        var toolCallbackProvider = new AsyncMcpToolCallbackProvider(reservationClients);

        return ChatClient.builder(chatModel)
                .defaultSystem(RESERVATION_SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .defaultTools(toolCallbackProvider)
                .build();
    }
}
