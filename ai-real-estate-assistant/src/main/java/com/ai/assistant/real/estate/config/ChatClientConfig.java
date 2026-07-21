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

    private static final String PROPERTY_SERVICE_NAME    = "property-service";
    private static final String RESERVATION_SERVICE_NAME = "reservation-service";

    // -------------------------------------------------------------------------
    // System prompts
    // -------------------------------------------------------------------------

    private static final String FAQ_SYSTEM_PROMPT = """
            You are the FAQ Agent for a real estate agency.
            Answer only using the information retrieved by your RAG tools.
            If no relevant information is found in the tool response, say you don't know.
            Never answer from your own knowledge or respond to unrelated topics.
            Reply in plain, natural language — never output JSON.
            """;

    private static final String PROPERTY_SYSTEM_PROMPT = """
            You are the Property Agent for a real estate agency.
            Answer only through your property MCP tools.
            Only present data returned by the tools. Never answer from your own knowledge or respond to unrelated topics.

            Always reply with a single JSON object — no text outside it, no markdown fences.

            Conversational reply:
            {"type":"text","message":"your answer here","properties":[]}

            Property results (use for any property returned by a tool):
            {"type":"property_list","message":"brief intro","properties":[{"referenceCode":"","title":"","description":"","propertyType":"","listingType":"","status":"","price":0,"currency":"","bedrooms":0,"bathrooms":0,"area":0,"city":"","country":"","address":"","imageUrls":[],"amenities":[]}]}
            """;

    private static final String RESERVATION_SYSTEM_PROMPT = """
            You are the Reservation Agent for a real estate agency.
            Answer only through your reservation MCP tools.
            Only present and answer based on the data returned by the tools.
            Never answer from your own knowledge or respond to unrelated topics.
            Reply in plain, natural language — never output JSON.
            """;

    // -------------------------------------------------------------------------
    // Per-agent ChatMemory beans
    // Each agent gets its own ChatMemory so conversation histories are fully
    // isolated: an FAQ exchange cannot bleed into a property or reservation
    // exchange, even when the user sends the same conversationId.
    // -------------------------------------------------------------------------

    @Bean
    @Qualifier("faqChatMemory")
    ChatMemory faqChatMemory(JdbcTemplate jdbcTemplate) {
        return buildChatMemory(jdbcTemplate);
    }

    @Bean
    @Qualifier("propertyChatMemory")
    ChatMemory propertyChatMemory(JdbcTemplate jdbcTemplate) {
        return buildChatMemory(jdbcTemplate);
    }

    @Bean
    @Qualifier("reservationChatMemory")
    ChatMemory reservationChatMemory(JdbcTemplate jdbcTemplate) {
        return buildChatMemory(jdbcTemplate);
    }

    private ChatMemory buildChatMemory(JdbcTemplate jdbcTemplate) {
        var repository = JdbcChatMemoryRepository.builder()
                .jdbcTemplate(jdbcTemplate)
                .dialect(new PostgresChatMemoryRepositoryDialect())
                .build();

        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(30)
                .build();
    }

    // -------------------------------------------------------------------------
    // Specialized agents
    // -------------------------------------------------------------------------

    @Bean
    @Qualifier("faqAgent")
    ChatClient faqAgent(OpenAiChatModel chatModel,
                        @Qualifier("faqChatMemory") ChatMemory faqChatMemory,
                        VectorStore vectorStore) {

        // 0.5 similarity threshold is appropriate for text-embedding-3-small:
        // cosine similarity for semantically related but paraphrased questions
        // typically falls in the 0.5–0.75 range with this model. The previous
        // threshold of 0.8 was too strict, causing the advisor to return no
        // documents even when relevant FAQs were present in the vector store.
        var faqAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder()
                        .similarityThreshold(0.5d)
                        .topK(6)
                        .build())
                .build();

        return ChatClient.builder(chatModel)
                .defaultSystem(FAQ_SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(faqChatMemory).build(),
                        faqAdvisor
                )
                .build();
    }

    @Bean
    @Qualifier("propertyAgent")
    ChatClient propertyAgent(OpenAiChatModel chatModel,
                             @Qualifier("propertyChatMemory") ChatMemory propertyChatMemory,
                             List<McpAsyncClient> mcpAsyncClients) {

        List<McpAsyncClient> propertyClients = mcpAsyncClients.stream()
                .filter(c -> PROPERTY_SERVICE_NAME.equals(c.getServerInfo().name()))
                .toList();

        var toolCallbackProvider = new AsyncMcpToolCallbackProvider(propertyClients);

        return ChatClient.builder(chatModel)
                .defaultSystem(PROPERTY_SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(propertyChatMemory).build()
                )
                .defaultTools(toolCallbackProvider)
                .build();
    }

    @Bean
    @Qualifier("reservationAgent")
    ChatClient reservationAgent(OpenAiChatModel chatModel,
                                @Qualifier("reservationChatMemory") ChatMemory reservationChatMemory,
                                List<McpAsyncClient> mcpAsyncClients) {

        List<McpAsyncClient> reservationClients = mcpAsyncClients.stream()
                .filter(c -> RESERVATION_SERVICE_NAME.equals(c.getServerInfo().name()))
                .toList();

        var toolCallbackProvider = new AsyncMcpToolCallbackProvider(reservationClients);

        return ChatClient.builder(chatModel)
                .defaultSystem(RESERVATION_SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(reservationChatMemory).build()
                )
                .defaultTools(toolCallbackProvider)
                .build();
    }
}
