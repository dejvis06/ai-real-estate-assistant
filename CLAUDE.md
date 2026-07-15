# Infrastructure - FAQ & Property Search

# ai-real-estate-assistant

It's an AI module, which will include:

- Web Server (Jetty)
- MCP Client (already added as a dependency - reactive)
- ChatModel (OpenAI)
- ChatMemory (JDBC)
- RAG (PGvector)
- Liquibase Schema and Initial Data
- Logging
- Dockerfile & docker-compose

---

## Web Server (Jetty)

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-webmvc</artifactId>
	<exclusions>
		<!-- Exclude the Tomcat dependency -->
		<exclusion>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-tomcat</artifactId>
		</exclusion>
	</exclusions>
</dependency>

<!-- Use Jetty instead -->

<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-jetty</artifactId>
</dependency>
```

---

## ChatModel (OpenAI)

```java
@Configuration
class ChatClientConfig {

	private static String systemPrompt = ""; // TODO complete, constrain the model, make it answer only for related functionalities

    @Bean
    ChatClient assistant(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem(systemPrompt)
                .build();
    }
}
```

---

## ChatMemory (JDBC)

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-chat-memory-repository-jdbc</artifactId>
</dependency>
```

```java
ChatMemoryRepository chatMemoryRepository = JdbcChatMemoryRepository.builder()
    .jdbcTemplate(jdbcTemplate)
    .dialect(new PostgresChatMemoryRepositoryDialect())
    .build();

ChatMemory chatMemory = MessageWindowChatMemory.builder()
    .chatMemoryRepository(chatMemoryRepository)
    .maxMessages(30)
    .build();
```

---

## RAG (PGvector)

### Advisor

```xml
<dependency>
   <groupId>org.springframework.ai</groupId>
   <artifactId>spring-ai-vector-store-advisor</artifactId>
</dependency>
```

Use the QuestionAnswerAdvisor.

```java
var faqAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
        .searchRequest(SearchRequest.builder().similarityThreshold(0.8d).topK(6).build())
        .build();
```

and embed on the ChatClient configs above.

---

### Vector Store

```xml
<dependency>
	<groupId>org.springframework.ai</groupId>
	<artifactId>spring-ai-starter-vector-store-pgvector</artifactId>
</dependency>
```

```java
@Bean
public VectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
    return PgVectorStore.builder(jdbcTemplate, embeddingModel)
        .indexType(HNSW)                     // Optional: defaults to HNSW
        .initializeSchema(true)              // Optional: defaults to false
        .schemaName("public")                // Optional: defaults to "public"
        .vectorTableName("vector_store")     // Optional: defaults to "vector_store"
        .build();
}
```

---

## FAQ Model

```text
Faq
---
id                  // Unique identifier (e.g. 1)
question            // The question customers commonly ask (e.g. "How do I schedule a property viewing?")
answer              // The answer presented to the customer (e.g. "You can request a viewing directly from the property page.")

category            // Groups similar FAQs (e.g. Buying, Selling, Financing, Company)
keywords            // Terms used to improve search and AI retrieval (e.g. ["viewing", "appointment", "visit"])
active              // Indicates whether the FAQ is publicly available (e.g. true)

createdAt           // Timestamp when the FAQ was created (e.g. 2026-07-10T14:30:00)
updatedAt           // Timestamp of the most recent modification (e.g. 2026-07-15T09:45:00)
```

---

# FAQ Management

The Assistant Service is responsible for managing FAQs through REST APIs.

FAQ records are stored in PostgreSQL. Every change is synchronized with the Spring AI PgVector so the AI assistant can retrieve FAQs using semantic search.

### REST Endpoints

```text
GET    /api/faqs
GET    /api/faqs/{id}

POST   /api/faqs
PUT    /api/faqs/{id}
DELETE /api/faqs/{id}
```

These endpoints are intended for internal users (e.g. managers/admins).

Generate also the required application layer for implementing these endpoints and use Spring Data JPA + PostgreSQL for all administrative CRUD operations.

---

# property-service

The MCP-service to be called by the ai-real-estate-assistant's MCP-client

It will include:

- Web Server (Jetty)
- MCP Server
- Spring Data JPA
- PostgreSQL
- Liquibase Schema and Initial Data
- Logging
- Dockerfile & docker-compose

---

## Web Server (Jetty)

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-webmvc</artifactId>
	<exclusions>
		<!-- Exclude the Tomcat dependency -->
		<exclusion>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-tomcat</artifactId>
		</exclusion>
	</exclusions>
</dependency>

<!-- Use Jetty instead -->

<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-jetty</artifactId>
</dependency>
```

---

## Property Model

```text
Property
--------
id                  // Unique identifier
referenceCode       // Public property reference (e.g. PROP-1001)
title               // Short listing title
description         // Detailed property description

propertyType        // Apartment, House, Villa, Land, Office...
listingType         // Sale, Rent
status              // Available, Reserved, Sold, Rented

price               // Listing price
currency            // EUR, USD, etc.

bedrooms            // Number of bedrooms
bathrooms           // Number of bathrooms
area                // Total area (m²)
floor               // Floor where the property is located
totalFloors         // Total floors in the building
yearBuilt           // Construction year

address             // Street address
city                // City
country             // Country
latitude            // GPS north/south coordinate
longitude           // GPS east/west coordinate

agentId             // Assigned real estate agent

createdAt           // Record creation timestamp
updatedAt           // Last update timestamp
```

---

```text
PropertyImage
-------------
id                  // Unique identifier
propertyId          // Property this image belongs to
url                 // Image URL or storage path
displayOrder        // Display order in the gallery
isPrimary           // Main image shown in listings
createdAt           // Upload timestamp
```

---

```text
PropertyAmenity
---------------
id                  // Unique identifier
propertyId          // Associated property
name                // Amenity name (Pool, Gym, Garden...)
createdAt           // Creation timestamp
```

---

```text
PropertyNearbyPlace
-------------------
id                  // Unique identifier
propertyId          // Associated property
name                // Place name
type                // School, Hospital, Park, Metro...
distance            // Distance from property
distanceUnit        // m, km
latitude            // GPS north/south coordinate
longitude           // GPS east/west coordinate
```

# Domain-Driven Design (DDD)

Use Domain-Driven Design (DDD) when generating the project structure.

Each bounded context should be organized independently and follow the same architecture.

Example:

```
<bounded-context>
├─ domain
│  ├─ model
│  │  ├─ AggregateRoot.java
│  │  ├─ Entity.java
│  │  ├─ ValueObject.java
│  │  ├─ Enum.java
│  │  └─ Identifier.java
│  │
│  ├─ repository
│  │  └─ Repository.java
│  │
│  └─ service
│     ├─ DomainService.java
│     └─ DefaultDomainService.java
│
├─ application
│  ├─ dto
│  │  ├─ Request.java
│  │  └─ Response.java
│  │
│  ├─ port
│  │  ├─ ExternalServiceClient.java
│  │  └─ AnotherExternalServiceClient.java
│  │
│  └─ service
│     └─ ApplicationService.java
│
├─ infrastructure
│  ├─ persistence
│  │  ├─ entity
│  │  │  ├─ JpaEntity.java
│  │  │  └─ ChildJpaEntity.java
│  │  │
│  │  ├─ mapper
│  │  │  └─ Mapper.java
│  │  │
│  │  ├─ repository
│  │  │  ├─ SpringDataRepository.java
│  │  │  └─ RepositoryImpl.java
│  │
│  └─ client
│     ├─ HttpExternalServiceClient.java
│     └─ HttpAnotherExternalServiceClient.java
│
└─ interface
   └─ rest
      └─ Controller.java
```


## Domain Layer

The domain layer represents the business model.

It should contain:

- Aggregates
- Entities
- Value Objects
- Domain Services
- Repository interfaces
- Domain Events (when applicable)
- Business rules and invariants

The domain layer must not depend on Spring, JPA, HTTP, or infrastructure concerns.

## Application Layer

The application layer coordinates use cases.

It is responsible for:

- orchestrating domain objects
- invoking domain services
- calling repositories
- communicating with external systems through ports
- transaction boundaries
- mapping requests and responses

Business rules should remain in the domain layer whenever possible.

## Infrastructure Layer

The infrastructure layer contains technical implementations.

Typical components include:

- JPA entities
- Spring Data repositories
- Repository implementations
- Mappers
- HTTP clients
- MCP clients/servers
- Messaging
- Database configuration
- External integrations

Infrastructure implements interfaces defined by the domain or application layers.

## Interface Layer

The interface layer exposes the application.

Typical components include:

- REST Controllers
- MCP Tools
- GraphQL Controllers
- Messaging Consumers
- Scheduled Jobs

Controllers should delegate directly to the application layer.

## Persistence

Separate the business model from the persistence model.

Use:

- Domain model for business behavior
- JPA entities for database persistence
- Mappers to translate between the two

Do not annotate domain models with JPA annotations.

## Dependencies

Dependencies should always point inward.

```
Interface
    ↓
Application
    ↓
Domain

Infrastructure
    ↑
implements Domain/Application interfaces
```

The domain layer should have no dependency on any outer layer.

## General Guidelines

- Organize the project by bounded context, not by technical layer.
- Each bounded context should own its domain model.
- Use constructor injection.
- Keep business rules inside the domain.
- Keep orchestration inside the application layer.
- Keep technical concerns inside the infrastructure layer.
- Keep controllers thin.
- Follow SOLID principles.
- Generate production-ready code.

# Agent Refactor Guide

Refactor the Real Estate AI Assistant into three specialized AI agents.

Each agent must receive only the context, MCP tools, and responsibilities required for its domain. The goal is to minimize prompt size, reduce unnecessary tool access, and improve response quality.

---

## Specialized Agents

Create the following agents:

- `FaqAgent`
- `PropertyAgent`
- `ReservationAgent`

Each agent should have its own `ChatClient` configured independently.

---

## MCP Configuration

Use **asynchronous MCP clients** throughout the application.

```java
McpAsyncClient
```

Use the asynchronous Spring AI callback provider:

```java
AsyncMcpToolCallbackProvider
```

Spring AI supports synchronous and asynchronous MCP clients, but **all configured MCP clients must use the same type**.

Configure the application explicitly as:

```yaml
spring:
  ai:
    mcp:
      client:
        type: ASYNC
```

---

## Agent Responsibilities

### FAQ Agent

Responsible only for company knowledge.

It answers questions using the Vector Store (RAG).

Examples:

- Company information
- Buying process
- Selling process
- Financing information
- Frequently asked questions
- General guidance

This agent **must not** perform:

- Property searches
- Reservation lookups
- Live business operations

It should have access only to:

- RAG Advisor / Vector Store

No MCP tools should be configured for this agent.

---

### Property Agent

Responsible only for live property operations.

Examples:

- Search properties
- Retrieve property details
- Compare properties
- List amenities
- Nearby places
- Availability
- Pricing

It should have access only to:

- Property MCP tools

It must not answer reservation questions.

---

### Reservation Agent

Responsible only for reservation operations.

Examples:

- Reservation details
- Reservation status
- Viewing schedule
- Cancellation
- Rescheduling
- Reservation policies

It should have access only to:

- Reservation MCP tools

It must not answer property search questions.

---

### System Prompts

Generate focused system prompts for:

- `FaqAgent`
- `PropertyAgent`
- `ReservationAgent`

The prompts must be based on:

- Available MCP tools
- Responsibility boundaries
- Supported operations

Each prompt must explicitly prevent the agent from performing work outside its domain.

---

### MCP Client Configuration

Replace the deprecated SSE configuration:

```yaml
spring:
  ai:
    mcp:
      client:
        sse:
          connections:
            property-service:
              url: ${MCP_PROPERTY_SERVICE_URL:http://localhost:8081}
```

with asynchronous Streamable HTTP connections:

```yaml
spring:
  ai:
    mcp:
      client:
        type: ASYNC
        streamable-http:
          connections:
            property-service:
              url: ${MCP_PROPERTY_SERVICE_URL:http://localhost:8081}

            reservation-service:
              url: ${MCP_RESERVATION_SERVICE_URL:http://localhost:8082}
```

---

### ChatService Routing

Inspect the existing `ChatService` and its routing logic.

Refactor it so that `ChatService` delegates requests to the appropriate specialized agent.

The routing logic should remain inside (or be coordinated by) `ChatService`.

The intended flow is:

```text
ChatController
      │
      ▼
ChatService
      │
      ├── FAQ request ─────────► FaqAgent
      │
      ├── Property request ────► PropertyAgent
      │
      └── Reservation request ─► ReservationAgent
```

---

### Final Architecture

```text
                         ┌───────────────────┐
User request ──────────► │    ChatService    │
                         │      routing      │
                         └─────────┬─────────┘
                                   │
              ┌────────────────────┼────────────────────┐
              │                    │                    │
              ▼                    ▼                    ▼
       ┌────────────┐       ┌───────────────┐    ┌──────────────────┐
       │  FAQ Agent │       │ Property Agent│    │ Reservation Agent│
       └─────┬──────┘       └───────┬───────┘    └────────┬─────────┘
             │                      │                     │
             ▼                      ▼                     ▼
       RAG Advisor         Property MCP Tools     Reservation MCP Tools
```

This separation ensures:

- **FAQ Agent**
    - Company knowledge through RAG.

- **Property Agent**
    - Live property operations through the Property MCP Server.

- **Reservation Agent**
    - Live reservation operations through the Reservation MCP Server.

Each model receives a smaller, more relevant context, while MCP tool access is constrained by the `ChatClient` configuration rather than relying solely on prompt instructions.

---

# Reservation Service Guide

The Reservation Service is responsible for managing property viewing reservations.

It exposes:

- REST APIs for reservation management.
- An MCP Server exposing reservation tools for the AI Assistant.

The service should include:

- Spring Boot
- Jetty Web Server
- Spring AI MCP Server
- Spring Data JPA
- PostgreSQL
- Liquibase (schema + initial data)
- Logging
- Dockerfile
- Docker Compose integration

---

### REST API

```text
POST /api/reservations
GET  /api/reservations/{id}
PUT  /api/reservations/{id}
```

No DELETE endpoint should exist.

Reservations are never physically removed.

Cancellation is represented by the reservation status.

---

### Domain Model

A reservation references a property managed by the Property Service.

Reuse (in the sense create the same structure/class) the existing Property Service response DTO:

```java
import ...property.application.dto.PropertyResponse;
```

The reservation details response should resemble:

```java
public record ReservationDetailsResponse(

        Long id,

        String reservationNumber,

        ReservationStatus status,

        PropertyResponse property,

        ReservationScheduleResponse schedule,

        ReservationPolicyResponse policy,

        String customerMessage,

        String internalNotes,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}
```


#### Reservation Policy

Business rules must be evaluated by the Reservation Service.

Clients and AI consumers must never calculate whether an action is allowed.

Instead, expose an evaluated policy.

```java
public record ReservationPolicyResponse(

        boolean canCancel,

        boolean canReschedule,

        LocalDateTime cancellationDeadline,

        CancellationFeeResponse cancellationFee,

        List<String> restrictions
) {
}
```

---

#### Cancellation Fee

```java
public record CancellationFeeResponse(

        boolean applicable,

        BigDecimal amount,

        String currency,

        String reason
) {
}
```

So therefore the domain models must reflect these response structures.

### MCP Tool

Expose an MCP tool for retrieving reservation details.

```java
@Tool(description = """
Retrieves reservation details by reservation ID.
Use this when the customer asks about an existing viewing reservation.
""")
public ReservationDetailsResponse getReservation(Long reservationId) {
    ...
}
```

The returned response should include:

- Reservation information
- Property information (`PropertyResponse` from the Property Service)
- Customer information
- Viewing schedule
- Evaluated reservation policies

This enables the AI Assistant to answer questions such as:

- Can I cancel my reservation?
- Will I be charged if I cancel?
- Can I reschedule?
- Why can't I modify my reservation?
- When is my viewing scheduled?


### Property Service Integration

The Reservation Service must retrieve property information from the Property Service through REST.

Expose the following endpoint in the Property Service:

```text
GET /api/properties/{id}
```

The endpoint should return:

```java
PropertyResponse
```

The Reservation Service should call this endpoint whenever reservation details require property information, embedding the returned `PropertyResponse` inside `ReservationDetailsResponse`.