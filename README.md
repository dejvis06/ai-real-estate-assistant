# AI Real Estate Assistant

An AI-powered real estate assistant built with **Spring Boot** and **Spring AI** that combines **Retrieval-Augmented Generation (RAG)** and **Model Context Protocol (MCP)** to help customers get answers about the company, discover properties, and manage viewing reservations.

## Features

- 💬 **FAQ Agent** — answers company, buying, selling, and financing questions using RAG over a curated FAQ knowledge base
- 🏡 **Property Agent** — searches and retrieves live property listings via MCP
- 📅 **Reservation Agent** — retrieves reservation details, viewing schedules, and evaluated cancellation/rescheduling policies via MCP

## Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                  ai-real-estate-assistant                    │
│                                                              │
│   ChatController ──► ChatService (routing)                   │
│                           │                                  │
│              ┌────────────┼────────────────┐                 │
│              ▼            ▼                ▼                 │
│         FaqAgent    PropertyAgent   ReservationAgent         │
│         (RAG)       (MCP tools)     (MCP tools)              │
│              │            │                │                 │
│         VectorStore  property-svc    reservation-svc         │
│         (pgvector)   :8081 (MCP)     :8082 (MCP)            │
│                                                              │
│   FAQ REST API: /api/faqs                                    │
└──────────────────────────────────────────────────────────────┘
          │                        │
┌─────────▼────────┐    ┌──────────▼──────────┐
│  property-service│    │ reservation-service  │
│  :8081           │    │ :8082                │
│  MCP Server      │    │ MCP Server           │
│  + REST API      │    │ + REST API           │
│  PostgreSQL      │◄───┤ PostgreSQL           │
│  property_db     │    │ reservation_db       │
└──────────────────┘    └─────────────────────┘
          │
┌─────────▼──────────────────────────────────┐
│   PostgreSQL (pgvector/pgvector:pg17)       │
│   • ai_assistant_db  (FAQs + vector store) │
│   • property_db      (Properties)          │
│   • reservation_db   (Reservations)        │
└────────────────────────────────────────────┘
```

**Modules**

| Module | Port | Description |
|---|---|---|
| `ai-real-estate-assistant` | 8080 | AI chat service with three specialized agents |
| `property-service` | 8081 | MCP server exposing property search tools |
| `reservation-service` | 8082 | MCP server exposing reservation tools |

---

## Agents

### FAQ Agent

Answers questions using **RAG** over the FAQ knowledge base stored in PGVector.

- Similarity threshold: `0.5` · Top-K: `6` (uses `text-embedding-3-small`)
- Isolated chat memory: `faq:{conversationId}`
- Responds in plain natural language

**Handles:**
- Company information and business hours
- Buying and selling process
- Financing and mortgage options
- General real estate guidance

---

### Property Agent

Answers questions about live property listings using **MCP tools** from `property-service`.

- Isolated chat memory: `property:{conversationId}`
- Responds in structured JSON parsed by the backend into `PropertyResponse`

**MCP Tools exposed by `property-service`:**

| Tool | Description |
|---|---|
| `searchProperties` | Search listings with optional filters: title, description, propertyType, listingType, status, city, country, price range, bedrooms, bathrooms, area, floor, yearBuilt |
| `getPropertyByReferenceCode` | Retrieve full property details by reference code (e.g. `PROP-1001`) including images, amenities, and nearby places |

---

### Reservation Agent

Answers questions about viewing reservations using **MCP tools** from `reservation-service`.

- Isolated chat memory: `reservation:{conversationId}`
- Reservation ID is automatically injected from the UI into the prompt
- Responds in plain natural language

**MCP Tools exposed by `reservation-service`:**

| Tool | Description |
|---|---|
| `getReservation` | Retrieve full reservation details by ID including property info, viewing schedule, and evaluated policy (canCancel, canReschedule, cancellationFee, restrictions) |

**Reservation policy fields returned by the tool:**

| Field | Description |
|---|---|
| `canCancel` | Whether the reservation can currently be cancelled |
| `canReschedule` | Whether the reservation can currently be rescheduled |
| `cancellationDeadline` | Deadline after which cancellation is no longer allowed |
| `cancellationFee` | Fee details if a cancellation charge applies |
| `restrictions` | List of reasons explaining why an action is not permitted |

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

This starts PostgreSQL (with pgvector), `property-service`, `reservation-service`, and `ai-real-estate-assistant` in the correct dependency order with health checks.

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

Then create the three databases and enable the vector extension:

```sql
CREATE DATABASE ai_assistant_db;
CREATE DATABASE property_db;
CREATE DATABASE reservation_db;
\c ai_assistant_db
CREATE EXTENSION IF NOT EXISTS vector;
```

### 2. Start property-service

```bash
cd property-service
./mvnw spring-boot:run
```

Liquibase runs on startup and creates the property tables and seeds 3 sample properties.

### 3. Start reservation-service

```bash
cd reservation-service
./mvnw spring-boot:run
```

Liquibase runs on startup and creates the reservation tables and seeds 3 sample reservations.

### 4. Start ai-real-estate-assistant

```bash
cd ai-real-estate-assistant
export OPENAI_API_KEY=sk-...
./mvnw spring-boot:run
```

Liquibase runs on startup and creates the FAQ tables and seeds 8 FAQs. On startup, all active FAQs are automatically synced to the PGVector store.

---

## REST APIs

### Chat — `ai-real-estate-assistant` :8080

```bash
# FAQ agent
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -H "X-Conversation-Id: session-1" \
  -d '{"message": "What documents do I need to buy a property?", "agentType": "FAQ"}'

# Property agent
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -H "X-Conversation-Id: session-1" \
  -d '{"message": "Show me apartments for sale under 200000 EUR", "agentType": "PROPERTY"}'

# Reservation agent (reservationId injected from UI)
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -H "X-Conversation-Id: session-1" \
  -d '{"message": "Can I cancel my reservation?", "agentType": "RESERVATION", "reservationId": 1}'
```

**Response shapes:**

```json
// FAQ / Reservation → TextResponse
{ "message": "You can request a viewing directly from the property page..." }

// Property (conversational) → PropertyResponse
{ "type": "text", "message": "I found the following properties...", "properties": [] }

// Property (listings) → PropertyResponse
{ "type": "property_list", "message": "Here are the results:", "properties": [ { "referenceCode": "PROP-1001", ... } ] }
```

---

### FAQ Management — `ai-real-estate-assistant` :8080

Every write operation automatically syncs the FAQ to the PGVector store.

```bash
# List all FAQs
curl http://localhost:8080/api/faqs

# Get a single FAQ
curl http://localhost:8080/api/faqs/1

# Create a FAQ
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

---

### Reservations — `reservation-service` :8082

```bash
# Get reservation details
curl http://localhost:8082/api/reservations/1

# Create a reservation
curl -X POST http://localhost:8082/api/reservations \
  -H "Content-Type: application/json" \
  -d '{
    "propertyId": 1,
    "customerName": "John Smith",
    "customerEmail": "john.smith@example.com",
    "customerPhone": "+355 69 123 4567",
    "viewingDateTime": "2026-08-01T10:00:00",
    "durationMinutes": 60,
    "customerMessage": "I am interested in purchasing this apartment."
  }'

# Update a reservation
curl -X PUT http://localhost:8082/api/reservations/1 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "CONFIRMED",
    "viewingDateTime": "2026-08-02T11:00:00",
    "agentNotes": "Meet at the main entrance."
  }'
```

> Reservations are never deleted. Cancellation is represented by setting `status` to `CANCELLED`.

---

## Seed Data

### Properties (`property_db`)

| Ref | Title | Type | Listing | Price | City |
|---|---|---|---|---|---|
| PROP-1001 | Modern 2BR Apartment in City Centre | APARTMENT | SALE | €185,000 | Tirana |
| PROP-1002 | Luxury Villa with Pool | VILLA | RENT | €4,500/mo | Florence |
| PROP-1003 | Prime Office Space – Central London | OFFICE | SALE | £1,250,000 | London |

### Reservations (`reservation_db`)

| Ref | Status | Property | Customer | Viewing |
|---|---|---|---|---|
| RES-2026-1001 | CONFIRMED | PROP-1001 | John Smith | +3 days |
| RES-2026-1002 | PENDING | PROP-1002 | Maria Rossi | +7 days |
| RES-2026-1003 | CANCELLED | PROP-1003 | James Thornton | -2 days |

### FAQs (`ai_assistant_db`)

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

**FAQ Agent**
- *What documents do I need to buy a property?*
- *What mortgage options are available?*
- *What is the typical down payment required?*
- *How long does the purchase process take?*

**Property Agent**
- *Show me available apartments for sale in Tirana.*
- *Are there any villas for rent in Italy?*
- *Find properties under €200,000 with at least 2 bedrooms.*
- *Tell me more about PROP-1001.*

**Reservation Agent**
- *What is the status of my reservation?*
- *When is my viewing scheduled?*
- *Can I cancel my reservation?*
- *Will I be charged if I cancel?*
- *Can I reschedule my viewing?*
