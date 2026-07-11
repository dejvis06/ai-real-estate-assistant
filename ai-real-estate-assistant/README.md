# ai-real-estate-assistant

Spring Boot service that provides an AI-powered real estate assistant. It answers natural language questions by combining three capabilities wired into a single `ChatClient`:

- **RAG** — retrieves relevant FAQs from a PGvector store and includes them as context
- **Chat Memory** — persists conversation history per session in PostgreSQL (JDBC)
- **MCP Client** — calls tools on `property-service` over SSE to fetch live property data

It also exposes a REST CRUD API for managing FAQs, with automatic synchronisation to the vector store on every write.

---

## Source Layout

```
src/main/java/com/ai/assistant/real/estate/
│
├── AiRealEstateAssistantApplication.java        # entry point
│
├── config/
│   ├── ChatClientConfig.java                    # ChatClient + ChatMemory beans
│   └── VectorStoreConfig.java                   # PgVectorStore bean
│
├── chat/                                        # bounded context: AI chat
│   ├── application/dto/
│   │   ├── ChatRequest.java                     # record { String message }
│   │   └── ChatResponse.java                    # record { String message }
│   └── interfaces/
│       ├── rest/ChatController.java             # POST /api/chat
│       └── cli/ChatCommandLineRunner.java       # interactive REPL (@Profile("cli"))
│
└── faq/                                         # bounded context: FAQ management
    ├── domain/
    │   ├── model/
    │   │   ├── Faq.java                         # aggregate root (no JPA annotations)
    │   │   ├── FaqId.java                       # value object  record FaqId(Long value)
    │   │   └── FaqCategory.java                 # enum: BUYING | SELLING | FINANCING | COMPANY
    │   └── repository/
    │       └── FaqRepository.java               # domain interface (save / findById / findAll / delete)
    ├── application/
    │   ├── dto/
    │   │   ├── FaqRequest.java                  # record (question, answer, category, keywords, active)
    │   │   └── FaqResponse.java                 # record + static FaqResponse.from(Faq)
    │   ├── port/
    │   │   └── FaqVectorStorePort.java          # interface (add / update / delete) — implemented in infra
    │   └── service/
    │       ├── FaqApplicationService.java       # orchestrates repo + vector store, @Transactional
    │       └── FaqNotFoundException.java
    ├── infrastructure/
    │   ├── persistence/
    │   │   ├── entity/FaqJpaEntity.java         # @Entity faq + @ElementCollection faq_keywords
    │   │   ├── mapper/FaqMapper.java            # FaqJpaEntity ↔ Faq
    │   │   └── repository/
    │   │       ├── FaqJpaRepository.java        # extends JpaRepository<FaqJpaEntity, Long>
    │   │       └── FaqRepositoryImpl.java       # implements FaqRepository
    │   └── vectorstore/
    │       └── FaqVectorStoreAdapter.java       # implements FaqVectorStorePort via VectorStore
    └── interfaces/
        └── rest/FaqController.java              # GET|POST|PUT|DELETE /api/faqs
```

---

## Configuration

```yaml
# src/main/resources/application.yml

spring:
  main:
    web-application-type: servlet         # force servlet mode (webflux MCP client is on classpath)

  datasource:
    url:      ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/ai_assistant_db}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}

  jpa:
    hibernate.ddl-auto: validate          # Liquibase owns the schema
    open-in-view: false

  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.yaml

  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat.options.model: gpt-5.2
      chat.options.temperature: 0
      embedding.options.model: text-embedding-3-small
    chat.memory.repository.jdbc.initialize-schema: always   # auto-creates chat memory table
    mcp.client.connections:
      property-service:
        type: sse
        url: ${MCP_PROPERTY_SERVICE_URL:http://localhost:8081/sse}

server:
  port: 8080
```

**Environment variables**

| Variable | Default | Required |
|---|---|---|
| `OPENAI_API_KEY` | — | yes |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/ai_assistant_db` | no |
| `DB_USERNAME` | `postgres` | no |
| `DB_PASSWORD` | `postgres` | no |
| `MCP_PROPERTY_SERVICE_URL` | `http://localhost:8081/sse` | no |

---

## Database Schema (Liquibase)

```
db/changelog/
├── db.changelog-master.yaml
├── 001-create-faq-table.yaml     # creates faq + faq_keywords tables
└── 002-faq-initial-data.yaml     # seeds 8 FAQs across 4 categories
```

**Tables managed by Liquibase**

```sql
faq (id, question, answer, category, active, created_at, updated_at)
faq_keywords (faq_id → faq.id, keyword)
```

**Tables managed by Spring AI auto-init**

```
spring_ai_chat_memory   — JDBC chat history (one row per message)
vector_store            — PGvector embeddings (auto-created by PgVectorStore.initializeSchema=true)
```

---

## Key Beans

### `VectorStoreConfig`
```java
// config/VectorStoreConfig.java
@Bean
VectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
    return PgVectorStore.builder(jdbcTemplate, embeddingModel)
            .indexType(PgVectorStore.PgIndexType.HNSW)
            .initializeSchema(true)   // creates vector_store table on first run
            .build();
}
```

### `ChatClientConfig`
```java
// config/ChatClientConfig.java
@Bean
ChatMemory chatMemory(JdbcTemplate jdbcTemplate) {
    // stores messages in spring_ai_chat_memory, keyed by conversationId
    return MessageWindowChatMemory.builder()
            .chatMemoryRepository(JdbcChatMemoryRepository.builder()
                    .jdbcTemplate(jdbcTemplate)
                    .dialect(new PostgresChatMemoryRepositoryDialect())
                    .build())
            .maxMessages(30)
            .build();
}

@Bean
ChatClient assistant(OpenAiChatModel model, ChatMemory memory,
                     VectorStore vectorStore, List<McpAsyncClient> mcpClients) {
    return ChatClient.builder(model)
            .defaultSystem("You are a professional real estate assistant ...")
            .defaultAdvisors(
                MessageChatMemoryAdvisor.builder(memory).build(),   // injects history
                QuestionAnswerAdvisor.builder(vectorStore)          // injects matching FAQs
                        .searchRequest(SearchRequest.builder()
                                .similarityThreshold(0.8).topK(6).build())
                        .build()
            )
            .defaultTools(new AsyncMcpToolCallbackProvider(mcpClients)) // property tools
            .build();
}
```

---

## Request Flow — Chat

```
User message
    │
    ▼
ChatController.chat()          POST /api/chat  { "message": "..." }
    │                          Header: X-Conversation-Id: <id>
    ▼
ChatClient.prompt(message)
    │
    ├─ MessageChatMemoryAdvisor
    │     reads last 30 messages for this conversationId from PostgreSQL
    │     appends them to the prompt as context
    │
    ├─ QuestionAnswerAdvisor
    │     embeds the user message → searches vector_store (similarity ≥ 0.8, top 6)
    │     prepends matching FAQ documents to the prompt
    │
    ├─ OpenAI GPT (gpt-5.2)
    │     the model may decide to call MCP tools:
    │
    │     └─ AsyncMcpToolCallbackProvider → SSE → property-service
    │           searchProperties(city, type, listingType, minPrice, maxPrice, minBedrooms)
    │           getPropertyByReferenceCode(referenceCode)
    │
    ▼
Response text
    │
    ▼
MessageChatMemoryAdvisor saves user + assistant messages to PostgreSQL
    │
    ▼
ChatController returns  { "message": "..." }
```

**Example I/O**

```
POST /api/chat
X-Conversation-Id: u-42

{ "message": "Are there any apartments for sale in Tirana under 200k?" }

→ { "message": "Yes, I found one listing:\n\n**PROP-1001 – Modern 2BR Apartment in City Centre**\nLocation: Rruga e Elbasanit 12, Tirana, Albania\nPrice: €185,000\nBedrooms: 2 | Bathrooms: 1 | Area: 75.5 m²\nFloor: 4 of 8 | Built: 2019\nAmenities: Elevator, Parking, Security System\n\nWould you like to know more or schedule a viewing?" }
```

```
POST /api/chat
X-Conversation-Id: u-42   ← same ID, memory is active

{ "message": "What documents will I need to buy it?" }

→ { "message": "To purchase that property you will typically need: a valid government-issued ID, proof of funds or mortgage pre-approval, tax identification number, and a signed sale agreement. Our agents will walk you through the full checklist." }
```
Note: the second reply references "that property" from memory, and the FAQ answer comes from RAG — no tool call needed.

---

## Request Flow — FAQ Write (vector store sync)

```
POST /api/faqs  { "question": "...", "answer": "...", "category": "BUYING", "keywords": [...] }
    │
    ▼
FaqController.createFaq()
    │
    ▼
FaqApplicationService.createFaq()   @Transactional
    ├─ Faq.create(...)              builds aggregate root
    ├─ FaqRepository.save()         persists to PostgreSQL via FaqRepositoryImpl
    │       FaqMapper.toEntity()    Faq → FaqJpaEntity
    │       FaqJpaRepository.save() Spring Data JPA
    │       FaqMapper.toDomain()    FaqJpaEntity → Faq  (with generated id)
    │
    └─ FaqVectorStorePort.add()     FaqVectorStoreAdapter
            builds Document:
              id      = "faq-{id}"
              content = "Question: ...\nCategory: ...\nAnswer: ...\nKeywords: ..."
            VectorStore.add()       embeds via text-embedding-3-small → stores in vector_store
    │
    ▼
201 Created  { "id": 9, "question": "...", "answer": "...", ... }
```

**Update and delete** follow the same path: the adapter calls `vectorStore.delete(["faq-{id}"])` first, then re-adds (update) or stops (delete).

---

## FAQ REST API

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/faqs` | List all FAQs |
| `GET` | `/api/faqs/{id}` | Get one FAQ |
| `POST` | `/api/faqs` | Create (syncs to vector store) |
| `PUT` | `/api/faqs/{id}` | Update (re-syncs vector store) |
| `DELETE` | `/api/faqs/{id}` | Delete (removes from vector store) |

**Request body (POST / PUT)**
```json
{
  "question": "Do you offer property management?",
  "answer":   "Yes, we offer full property management services.",
  "category": "COMPANY",
  "keywords": ["management", "landlord", "rental"],
  "active":   true
}
```

**Response body**
```json
{
  "id":        9,
  "question":  "Do you offer property management?",
  "answer":    "Yes, we offer full property management services.",
  "category":  "COMPANY",
  "keywords":  ["management", "landlord", "rental"],
  "active":    true,
  "createdAt": "2026-07-11T10:00:00",
  "updatedAt": "2026-07-11T10:00:00"
}
```

**404 response** (GET / PUT / DELETE on missing id)
```
FAQ not found with id: 99
```

---

## CLI Mode

The `ChatCommandLineRunner` activates under the `cli` Spring profile. It is a simple REPL loop that reuses the same `ChatClient` bean (full RAG + memory + MCP tools).

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=cli
```

```
==============================================
  AI Real Estate Assistant — CLI Mode
  Type your question and press Enter.
  Type 'exit' or 'quit' to stop.
==============================================

You: Show me villas for rent in Italy
Assistant: I found a match:

**PROP-1002 – Luxury Villa with Pool – Tuscany**
Location: Via della Vigna Nuova 18, Florence, Italy
Price: €4,500/month | Area: 280 m² | Bedrooms: 4 | Bathrooms: 3
Amenities: Swimming Pool, Garden, Terrace, BBQ Area, Olive Grove
Nearby: Florence Cathedral (2.5 km), Mercato Centrale (3 km)

Would you like more details or to arrange a viewing?

You: What is the down payment typically needed?
Assistant: Down payment requirements vary by country and loan type.
Generally, a minimum of 10–20% of the purchase price is required ...

You: exit
Goodbye!
```

The session uses conversationId `cli-session` — history accumulates across turns within one run but resets on restart.

---

## Seeded FAQs

| id | Category | Question |
|---|---|---|
| 1 | BUYING | How do I schedule a property viewing? |
| 2 | BUYING | What documents do I need to purchase a property? |
| 3 | BUYING | How long does the property purchase process take? |
| 4 | SELLING | How do I list my property for sale? |
| 5 | SELLING | How is my property price determined? |
| 6 | FINANCING | What mortgage options are available? |
| 7 | FINANCING | What is the typical down payment required? |
| 8 | COMPANY | What are your business hours and how can I contact an agent? |
