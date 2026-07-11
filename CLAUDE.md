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