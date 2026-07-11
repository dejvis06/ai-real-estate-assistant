# AI Real Estate Assistant

An AI-powered real estate assistant built with **Spring Boot** and **Spring AI** that combines **Retrieval-Augmented Generation (RAG)** and **Model Context Protocol (MCP)** to help customers discover properties and schedule viewings.

## Features

* 🔍 Search available properties using natural language
* 🏡 Retrieve detailed property and neighborhood information
* 📅 Request property viewing appointments
* 👨‍💼 Agent approval or cancellation of viewing requests
* 🤖 AI-powered responses grounded in company knowledge and live business data

## Architecture

* **Spring Boot**
* **Spring AI**
* **RAG** for property guides, neighborhood information, and buying documentation
* **MCP** for interacting with Property, Reservation, Calendar, and Notification services
* **PostgreSQL**
* **Docker**

## Example Questions

* *Show me available apartments near the city center under €200,000.*
* *Tell me more about this property.*
* *I'd like to schedule a viewing this Friday afternoon.*
* *Are there similar properties available?*

This project demonstrates how modern AI assistants can combine knowledge retrieval with real-time business operations to support real estate sales.


# Architecture

```
┌─────────────────────────────────────────────┐
│           ai-real-estate-assistant          │
│  ┌──────────────┐   ┌─────────────────────┐ │
│  │  ChatClient  │   │   FAQ REST API       │ │
│  │  + RAG       │   │   /api/faqs          │ │
│  │  + Memory    │   └─────────────────────┘ │
│  │  + MCP tools │                           │
│  └──────┬───────┘                           │
│         │ MCP (SSE)                         │
└─────────┼───────────────────────────────────┘
          │
┌─────────▼───────────────────────────────────┐
│             property-service                │
│   MCP Server exposing property search tools │
│   backed by PostgreSQL                      │
└─────────────────────────────────────────────┘
          │
┌─────────▼───────────────────────────────────┐
│   PostgreSQL (pgvector/pgvector:pg17)        │
│   • ai_assistant_db  (FAQs + vector store)  │
│   • property_db      (Properties)           │
└─────────────────────────────────────────────┘
```

**Modules**

| Module | Port | Description |
|---|---|---|
| `ai-real-estate-assistant` | 8080 | AI chat service with RAG + MCP client |
| `property-service` | 8081 | MCP server exposing property search |

---

## Prerequisites

- Java 25
- Maven (or use the included `mvnw` wrapper)
- Docker & Docker Compose
- An OpenAI API key

---

## Running with Docker Compose (recommended)

```bash
# From the repo root
export OPENAI_API_KEY=sk-...

docker compose up --build
```

This starts PostgreSQL (with pgvector), `property-service`, and `ai-real-estate-assistant` in the correct order with health checks.

---

## Running Locally (without Docker)

### 1. Start PostgreSQL with pgvector

```bash
docker run -d \
  --name ai-re-postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  pgvector/pgvector:pg17
```

Then create the two databases and enable the vector extension:

```sql
CREATE DATABASE ai_assistant_db;
CREATE DATABASE property_db;
\c ai_assistant_db
CREATE EXTENSION IF NOT EXISTS vector;
```

### 2. Start property-service

```bash
cd property-service
./mvnw spring-boot:run
```

Liquibase runs on startup and creates the property tables + seeds 3 sample properties.

### 3. Start ai-real-estate-assistant

```bash
cd ai-real-estate-assistant
export OPENAI_API_KEY=sk-...
./mvnw spring-boot:run
```

Liquibase runs on startup and creates the FAQ tables + seeds 8 sample FAQs. The PGvector schema is auto-initialized by Spring AI.

---

## Testing via Command Line (CLI mode)

The assistant includes an interactive CLI runner activated with the `cli` Spring profile. It keeps conversation memory across turns within the same session.

```bash
cd ai-real-estate-assistant
export OPENAI_API_KEY=sk-...

./mvnw spring-boot:run -Dspring-boot.run.profiles=cli
```

Example session:

```
==============================================
  AI Real Estate Assistant — CLI Mode
  Type your question and press Enter.
  Type 'exit' or 'quit' to stop.
==============================================

You: Are there any apartments for sale in Tirana?
Assistant: Yes! I found a listing in Tirana ...

You: How many bedrooms does it have?
Assistant: The apartment has 2 bedrooms ...

You: What documents do I need to buy it?
Assistant: To purchase a property you typically need ...

You: exit
Goodbye!
```

The CLI session uses a fixed conversation ID (`cli-session`), so follow-up questions are aware of what was said earlier in the same run.

---

## Testing via REST API

### Chat endpoint

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -H "X-Conversation-Id: test-1" \
  -d '{"message": "Show me apartments for sale under 200000 EUR"}'
```

Multi-turn — reuse the same `X-Conversation-Id` header to continue the conversation:

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -H "X-Conversation-Id: test-1" \
  -d '{"message": "Tell me more about the first one"}'
```

### FAQ endpoints

```bash
# List all FAQs
curl http://localhost:8080/api/faqs

# Get a single FAQ
curl http://localhost:8080/api/faqs/1

# Create a FAQ (syncs to vector store automatically)
curl -X POST http://localhost:8080/api/faqs \
  -H "Content-Type: application/json" \
  -d '{
    "question": "Do you offer property management services?",
    "answer": "Yes, we offer full property management including tenant screening, rent collection, and maintenance coordination.",
    "category": "COMPANY",
    "keywords": ["property management", "rental management", "landlord"]
  }'

# Update a FAQ
curl -X PUT http://localhost:8080/api/faqs/1 \
  -H "Content-Type: application/json" \
  -d '{
    "question": "How do I schedule a property viewing?",
    "answer": "Updated answer here.",
    "category": "BUYING",
    "keywords": ["viewing", "appointment"],
    "active": true
  }'

# Delete a FAQ
curl -X DELETE http://localhost:8080/api/faqs/1
```

### Test RAG — ask about a seeded FAQ topic

After startup, the 8 seed FAQs are in the vector store. Ask anything about them through the chat endpoint:

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -H "X-Conversation-Id: faq-test" \
  -d '{"message": "What documents do I need to buy a property?"}'
```

Expected: the assistant retrieves the matching FAQ via semantic search and answers accurately.

---

## Seed Data

### Properties (property-service)

| Ref | Title | Type | Listing | Price | City |
|---|---|---|---|---|---|
| PROP-1001 | Modern 2BR Apartment in City Centre | APARTMENT | SALE | €185,000 | Tirana |
| PROP-1002 | Luxury Villa with Pool – Tuscany | VILLA | RENT | €4,500/mo | Florence |
| PROP-1003 | Prime Office Space – Central London | OFFICE | SALE | £1,250,000 | London |

### FAQs (ai-real-estate-assistant)

| # | Category | Topic |
|---|---|---|
| 1 | BUYING | How to schedule a viewing |
| 2 | BUYING | Documents needed to purchase |
| 3 | BUYING | How long the purchase process takes |
| 4 | SELLING | How to list a property |
| 5 | SELLING | How the price is determined |
| 6 | FINANCING | Available mortgage options |
| 7 | FINANCING | Typical down payment required |
| 8 | COMPANY | Business hours and agent contact |

---

## Example Questions to Try

- *Show me available apartments for sale in Tirana.*
- *Are there any villas for rent in Italy?*
- *What is the reference code PROP-1001?*
- *What documents do I need to buy a property?*
- *How long does the purchase process take?*
- *What mortgage options are available?*
