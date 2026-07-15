# reservation-service

Spring Boot service that manages property viewing reservations. It exposes reservation tools over MCP (Streamable HTTP) that the `ai-real-estate-assistant` MCP client connects to and calls automatically during AI conversations. It also provides a REST API for direct reservation management.

It integrates with `property-service` via REST to enrich reservation details with live property information.

---

## Source Layout

```
src/main/java/com/reservation/
│
├── ReservationServiceApplication.java           # entry point
│
├── config/
│   ├── McpConfig.java                           # registers @Tool methods with the MCP server
│   └── DomainConfig.java                        # wires domain service beans (ReservationNumberGenerator)
│
└── reservation/                                 # bounded context: reservation
    ├── domain/
    │   ├── model/
    │   │   ├── Reservation.java                 # aggregate root (builder pattern, mutable status)
    │   │   ├── ReservationId.java               # value object  record ReservationId(Long value)
    │   │   ├── ReservationStatus.java           # enum: PENDING | CONFIRMED | CANCELLED | COMPLETED | NO_SHOW
    │   │   ├── ReservationSchedule.java         # value object (viewingDateTime, durationMinutes, agentNotes)
    │   │   ├── ReservationPolicy.java           # value object (canCancel, canReschedule, deadline, fee, restrictions)
    │   │   └── CancellationFee.java             # value object (applicable, amount, currency, reason)
    │   ├── repository/
    │   │   └── ReservationRepository.java       # domain interface
    │   └── service/
    │       ├── ReservationNumberGenerator.java  # domain service interface
    │       └── DefaultReservationNumberGenerator.java  # generates RES-{year}-{seq}
    ├── application/
    │   ├── dto/
    │   │   ├── ReservationDetailsResponse.java  # full response record (used by REST + MCP)
    │   │   ├── ReservationScheduleResponse.java
    │   │   ├── ReservationPolicyResponse.java
    │   │   ├── CancellationFeeResponse.java
    │   │   ├── PropertyResponse.java            # mirror of property-service DTO (populated via REST)
    │   │   ├── PropertyImageResponse.java
    │   │   ├── PropertyAmenityResponse.java
    │   │   ├── PropertyNearbyPlaceResponse.java
    │   │   ├── CreateReservationRequest.java
    │   │   └── UpdateReservationRequest.java
    │   ├── port/
    │   │   └── PropertyServiceClient.java       # outbound port: fetch property from property-service
    │   └── service/
    │       ├── ReservationApplicationService.java   # create / get / update reservation
    │       └── ReservationNotFoundException.java
    ├── infrastructure/
    │   ├── persistence/
    │   │   ├── entity/
    │   │   │   └── ReservationJpaEntity.java        # @Entity  table: reservation
    │   │   ├── mapper/
    │   │   │   └── ReservationMapper.java           # ReservationJpaEntity ↔ Reservation (domain)
    │   │   └── repository/
    │   │       ├── ReservationJpaRepository.java    # Spring Data JPA
    │   │       └── ReservationRepositoryImpl.java   # implements ReservationRepository
    │   └── client/
    │       └── HttpPropertyServiceClient.java       # implements PropertyServiceClient via RestClient
    └── interfaces/
        ├── mcp/
        │   └── ReservationMcpTool.java          # @Tool methods exposed over MCP
        └── rest/
            └── ReservationController.java       # POST /api/reservations, GET /{id}, PUT /{id}
```

---

## Configuration

```yaml
# src/main/resources/application.yml

spring:
  datasource:
    url:      ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/reservation_db}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}

  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.yaml

  ai:
    mcp:
      server:
        name: reservation-service
        version: 0.0.1-SNAPSHOT

clients:
  property-service:
    url: ${PROPERTY_SERVICE_URL:http://localhost:8081}

server:
  port: 8082
```

**Environment variables**

| Variable | Default | Required |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/reservation_db` | no |
| `DB_USERNAME` | `postgres` | no |
| `DB_PASSWORD` | `postgres` | no |
| `PROPERTY_SERVICE_URL` | `http://localhost:8081` | no |

---

## Database Schema (Liquibase)

```
db/changelog/
├── db.changelog-master.yaml
├── 001-create-reservation-table.yaml    # creates reservation table + indexes
└── 002-reservation-initial-data.yaml   # seeds 3 sample reservations
```

**Tables**

```sql
reservation  (id, reservation_number, status, property_id, customer_name, customer_email,
              customer_phone, viewing_date_time, duration_minutes, agent_notes,
              customer_message, internal_notes, created_at, updated_at)
```

Indexes on: `status`, `property_id`, `reservation_number`, `customer_email`.

Reservations are **never physically deleted**. Cancellation is represented by `status = CANCELLED`.

---

## REST API

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/reservations` | Create a new reservation |
| `GET` | `/api/reservations/{id}` | Get reservation details by ID |
| `PUT` | `/api/reservations/{id}` | Update status, schedule, or notes |

There is no `DELETE` endpoint. Cancellation is done via `PUT` with `status: CANCELLED`.

---

## MCP Tool

Registered in `McpConfig` via `MethodToolCallbackProvider` and exposed to any connected MCP client.

### `getReservation`

```
Retrieves full reservation details by reservation ID.
Used when the customer asks about an existing viewing reservation.

Parameters
  reservationId  Long    the numeric ID of the reservation

Returns
  ReservationDetailsResponse containing:
    - reservation info (number, status, schedule)
    - property info    (from property-service via REST)
    - evaluated policy (canCancel, canReschedule, cancellationDeadline, cancellationFee, restrictions)
```

The `policy` field is always evaluated server-side. AI consumers must never calculate policy themselves — they read `canCancel`, `canReschedule`, and `cancellationFee.applicable` directly from the response.

---

## Request Flow

```
MCP Client (ai-real-estate-assistant)
    │  Streamable HTTP connection to http://reservation-service:8082
    │
    ▼
ReservationMcpTool.getReservation(reservationId)
    │
    ▼
ReservationApplicationService.getReservationDetails(id)
    ├─ ReservationRepository.findById(id)
    │       ReservationRepositoryImpl
    │         ReservationJpaRepository.findById(id)
    │         ReservationMapper.toDomain()
    │
    ├─ PropertyServiceClient.findById(propertyId)
    │       HttpPropertyServiceClient
    │         GET http://property-service:8081/api/properties/{id}
    │         returns PropertyResponse (or empty on 404/error)
    │
    └─ reservation.evaluatePolicy()
           computes canCancel, canReschedule, cancellationDeadline
           applies 24-hour cancellation fee rule
           collects restriction messages
    │
    ▼
ReservationDetailsResponse → returned to MCP client as JSON
```

---

## I/O Examples

### Tool call: `getReservation`

**Input** (sent by the AI model via MCP)
```json
{ "reservationId": 1 }
```

**Output**
```json
{
  "id": 1,
  "reservationNumber": "RES-2026-1001",
  "status": "CONFIRMED",
  "property": {
    "id": 1,
    "referenceCode": "PROP-1001",
    "title": "Modern 2BR Apartment in City Centre",
    "propertyType": "APARTMENT",
    "listingType": "SALE",
    "status": "AVAILABLE",
    "price": 185000.00,
    "currency": "EUR",
    "city": "Tirana",
    "country": "Albania"
  },
  "schedule": {
    "viewingDateTime": "2026-07-18T10:00:00",
    "durationMinutes": 60,
    "agentNotes": "Meet at the main entrance. Key fob required for elevator."
  },
  "policy": {
    "canCancel": true,
    "canReschedule": true,
    "cancellationDeadline": "2026-07-17T10:00:00",
    "cancellationFee": {
      "applicable": false,
      "amount": null,
      "currency": null,
      "reason": null
    },
    "restrictions": []
  },
  "customerMessage": "I am interested in purchasing this apartment. Please confirm the viewing.",
  "internalNotes": null,
  "createdAt": "2026-07-15T09:00:00",
  "updatedAt": "2026-07-15T09:00:00"
}
```

### REST call: `POST /api/reservations`

**Input**
```json
{
  "propertyId": 1,
  "customerName": "John Smith",
  "customerEmail": "john.smith@example.com",
  "customerPhone": "+355 69 123 4567",
  "viewingDateTime": "2026-07-20T11:00:00",
  "durationMinutes": 60,
  "customerMessage": "I would like to view this apartment before making an offer."
}
```

**Output** — `201 Created`, body same shape as `getReservation` above with `status: PENDING`.

### REST call: `PUT /api/reservations/{id}` — cancel

**Input**
```json
{ "status": "CANCELLED" }
```

**Output** — `200 OK`, body with updated `status: CANCELLED` and policy reflecting `canCancel: false`.

---

## Domain Model Design Notes

**`Reservation` aggregate** owns all business rules. Policy evaluation (`evaluatePolicy()`) and state transitions (`cancel()`, `confirm()`) live entirely inside the domain model — no business logic leaks into the application or infrastructure layers.

**`ReservationPolicy` is computed on read**, not stored. Every call to `getReservationDetails` re-evaluates the policy from the current state and the current time. This keeps the database simple (no policy columns) and ensures the policy is always current.

**Cancellation fee rule**: if a cancellation is attempted within 24 hours of the scheduled viewing, `cancellationFee.applicable` is set to `true`. The fee amount is intentionally left null — pricing is a business concern to be filled in by a future billing integration.

**No DELETE endpoint** — reservations are a permanent audit record. `status = CANCELLED` is the cancellation mechanism.

**`PropertyResponse` is a local mirror** of the property-service DTO. There is no shared library dependency between services — the record is duplicated by design to keep services independently deployable.

**Separation of persistence and domain** — `ReservationJpaEntity` lives only in `infrastructure/persistence/entity`. `ReservationMapper` translates between it and the domain model. The domain `Reservation` class has zero Spring/JPA annotations.

---

## Seeded Reservations

| Ref | Status | Property | Customer | Viewing |
|---|---|---|---|---|
| RES-2026-1001 | CONFIRMED | PROP-1001 (Tirana Apt) | John Smith | +3 days from seed |
| RES-2026-1002 | PENDING | PROP-1002 (Tuscany Villa) | Maria Rossi | +7 days from seed |
| RES-2026-1003 | CANCELLED | PROP-1003 (London Office) | James Thornton | -2 days from seed |
