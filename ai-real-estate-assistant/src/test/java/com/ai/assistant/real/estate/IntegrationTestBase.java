package com.ai.assistant.real.estate;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = {
        // Satisfy OpenAI property binding — model is mocked so API is never called
        "spring.ai.openai.api-key=test",
        // Disable MCP client entirely — no SSE connection attempt to localhost:8081
        // (default initialized=true causes the SYNC client to block-handshake on startup)
        "spring.ai.mcp.client.enabled=false",
        // Skip pgvector schema init — VectorStore is mocked
        "spring.ai.vectorstore.pgvector.initialize-schema=false",
        // Let Liquibase own the schema; skip Hibernate validation in tests
        "spring.jpa.hibernate.ddl-auto=none"
})
public abstract class IntegrationTestBase {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @MockitoBean
    protected VectorStore vectorStore;

    @MockitoBean
    protected OpenAiChatModel openAiChatModel;

    @MockitoBean
    protected EmbeddingModel embeddingModel;

    @MockitoBean
    protected ToolCallbackProvider mcpToolCallbackProvider;
}
