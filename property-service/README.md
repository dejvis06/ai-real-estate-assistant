# property-service

Spring Boot service that acts as an **MCP server**. It exposes property search tools over SSE that the `ai-real-estate-assistant` MCP client connects to and calls automatically during AI conversations.

It has no REST API of its own — everything is exposed through MCP tools backed by Spring Data JPA + PostgreSQL.

---

## Source Layout

```
src/main/java/com/property/
│
├── PropertyServiceApplication.java              # entry point
│
├── config/
│   └── McpConfig.java                           # registers @Tool methods with the MCP server
│
└── property/                                    # bounded context: property
    ├── domain/
    │   ├── model/
    │   │   ├── Property.java                    # aggregate root (builder pattern, immutable)
    │   │   ├── PropertyId.java                  # value object  record PropertyId(Long value)
    │   │   ├── PropertyImage.java               # entity (id, url, displayOrder, isPrimary)
    │   │   ├── PropertyAmenity.java             # entity (id, name)
    │   │   ├── PropertyNearbyPlace.java         # entity (id, name, type, distance, lat/lon)
    │   │   ├── PropertyType.java                # enum: APARTMENT | HOUSE | VILLA | LAND | OFFICE
    │   │   ├── ListingType.java                 # enum: SALE | RENT
    │   │   ├── PropertyStatus.java              # enum: AVAILABLE | RESERVED | SOLD | RENTED
    │   │   └── PropertySearchCriteria.java      # value object record (all fields optional)
    │   └── repository/
    │       └── PropertyRepository.java          # domain interface
    ├── application/
    │   ├── dto/
    │   │   ├── PropertyResponse.java            # flat record + static .from(Property)
    │   │   ├── PropertyImageResponse.java
    │   │   ├── PropertyAmenityResponse.java
    │   │   └── PropertyNearbyPlaceResponse.java
    │   └── service/
    │       └── PropertyApplicationService.java  # searchProperties / getPropertyByReferenceCode
    ├── infrastructure/
    │   └── persistence/
    │       ├── entity/
    │       │   ├── PropertyJpaEntity.java           # @Entity  table: property
    │       │   ├── PropertyImageJpaEntity.java      # @Entity  table: property_image
    │       │   ├── PropertyAmenityJpaEntity.java    # @Entity  table: property_amenity
    │       │   └── PropertyNearbyPlaceJpaEntity.java# @Entity  table: property_nearby_place
    │       ├── mapper/
    │       │   └── PropertyMapper.java              # PropertyJpaEntity → Property (domain)
    │       └── repository/
    │           ├── PropertyJpaRepository.java       # Spring Data JPA + custom JPQL queries
    │           ├── PropertySpecification.java       # JPA Criteria for dynamic filtering
    │           └── PropertyRepositoryImpl.java      # implements PropertyRepository
    └── interfaces/
        └── mcp/
            └── PropertyMcpTool.java             # @Tool methods exposed over MCP
```

---

## Configuration

```yaml
# src/main/resources/application.yml

spring:
  datasource:
    url:      ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/property_db}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}

  jpa:
    hibernate.ddl-auto: validate          # Liquibase owns the schema
    open-in-view: false

  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.yaml

  ai:
    mcp:
      server:
        name: property-service
        version: 0.0.1-SNAPSHOT
        sse-message-endpoint: /sse/message   # SSE endpoint consumed by the MCP client

server:
  port: 8081
```

**Environment variables**

| Variable | Default | Required |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/property_db` | no |
| `DB_USERNAME` | `postgres` | no |
| `DB_PASSWORD` | `postgres` | no |

---

## Database Schema (Liquibase)

```
db/changelog/
├── db.changelog-master.yaml
├── 001-create-property-tables.yaml    # creates all 4 tables + indexes
└── 002-property-initial-data.yaml     # seeds 3 sample properties
```

**Tables**

```sql
property             (id, reference_code, title, description, property_type, listing_type,
                      status, price, currency, bedrooms, bathrooms, area, floor, total_floors,
                      year_built, address, city, country, latitude, longitude, agent_id,
                      created_at, updated_at)

property_image       (id, property_id → property.id, url, display_order, is_primary, created_at)
property_amenity     (id, property_id → property.id, name, created_at)
property_nearby_place(id, property_id → property.id, name, type, distance, distance_unit,
                      latitude, longitude)
```

Indexes on: `city`, `property_type`, `listing_type`, `status`, `price`, `bedrooms`.

---

## MCP Tools

Registered in `McpConfig` via `MethodToolCallbackProvider` and exposed to any connected MCP client.

### `searchProperties`

```
Finds available properties (status = AVAILABLE). All parameters are optional.

Parameters
  city          String          partial city name match (case-insensitive)
  propertyType  String          APARTMENT | HOUSE | VILLA | LAND | OFFICE
  listingType   String          SALE | RENT
  minPrice      BigDecimal      minimum price
  maxPrice      BigDecimal      maximum price
  minBedrooms   Integer         minimum bedroom count
```

### `getPropertyByReferenceCode`

```
Returns full details for a single property by its public reference code.

Parameters
  referenceCode  String         e.g. PROP-1001
```

---

## Request Flow

```
MCP Client (ai-real-estate-assistant)
    │  SSE connection to http://property-service:8081/sse
    │
    ▼
PropertyMcpTool.searchProperties(city, type, listingType, minPrice, maxPrice, minBedrooms)
    │
    ▼
PropertyApplicationService.searchProperties()
    ├─ builds PropertySearchCriteria record
    ├─ PropertyRepository.findByCriteria(criteria)
    │       PropertyRepositoryImpl
    │         ① PropertyJpaRepository.findAll(Specification)   — gets matching IDs (no collections)
    │         ② PropertyJpaRepository.findAllWithDetailsByIdIn(ids)
    │              JPQL: SELECT DISTINCT p ... LEFT JOIN FETCH images, amenities, nearbyPlaces
    │         PropertyMapper.toDomain()  for each entity
    │
    ▼
List<Property> (domain) → List<PropertyResponse> (DTO) → returned to MCP client as JSON
```

The two-step query (first IDs, then fetch with JOIN FETCH) avoids the Hibernate Cartesian product warning that occurs when joining multiple one-to-many collections in a single query.

---

## I/O Examples

### Tool call: `searchProperties`

**Input** (sent by the AI model via MCP)
```json
{
  "city": "Tirana",
  "listingType": "SALE",
  "maxPrice": 200000
}
```

**Output**
```json
[
  {
    "id": 1,
    "referenceCode": "PROP-1001",
    "title": "Modern 2BR Apartment in City Centre",
    "description": "A bright, fully furnished apartment in the heart of the city...",
    "propertyType": "APARTMENT",
    "listingType": "SALE",
    "status": "AVAILABLE",
    "price": 185000.00,
    "currency": "EUR",
    "bedrooms": 2,
    "bathrooms": 1,
    "area": 75.50,
    "floor": 4,
    "totalFloors": 8,
    "yearBuilt": 2019,
    "address": "Rruga e Elbasanit 12",
    "city": "Tirana",
    "country": "Albania",
    "latitude": 41.32754000,
    "longitude": 19.81869000,
    "agentId": 1,
    "images": [
      { "id": 1, "url": "https://images.example.com/prop-1001/main.jpg", "displayOrder": 0, "primary": true },
      { "id": 2, "url": "https://images.example.com/prop-1001/living.jpg", "displayOrder": 1, "primary": false }
    ],
    "amenities": [
      { "id": 1, "name": "Elevator" },
      { "id": 2, "name": "Parking" },
      { "id": 3, "name": "Security System" }
    ],
    "nearbyPlaces": [
      { "id": 1, "name": "Tirana International Airport", "type": "Airport", "distance": 17.00, "distanceUnit": "km" },
      { "id": 2, "name": "Blloku", "type": "Park", "distance": 0.80, "distanceUnit": "km" },
      { "id": 3, "name": "American Hospital", "type": "Hospital", "distance": 1.20, "distanceUnit": "km" }
    ],
    "createdAt": "2026-07-11T10:00:00",
    "updatedAt": "2026-07-11T10:00:00"
  }
]
```

### Tool call: `getPropertyByReferenceCode`

**Input**
```json
{ "referenceCode": "PROP-1002" }
```

**Output**
```json
{
  "id": 2,
  "referenceCode": "PROP-1002",
  "title": "Luxury Villa with Pool – Tuscany",
  "propertyType": "VILLA",
  "listingType": "RENT",
  "status": "AVAILABLE",
  "price": 4500.00,
  "currency": "EUR",
  "bedrooms": 4,
  "bathrooms": 3,
  "area": 280.00,
  "city": "Florence",
  "country": "Italy",
  "amenities": [
    { "id": 4, "name": "Swimming Pool" },
    { "id": 5, "name": "Garden" },
    { "id": 6, "name": "Terrace" },
    { "id": 7, "name": "BBQ Area" },
    { "id": 8, "name": "Olive Grove" }
  ],
  "nearbyPlaces": [
    { "id": 4, "name": "Florence Cathedral", "type": "Landmark", "distance": 2.50, "distanceUnit": "km" },
    { "id": 5, "name": "Mercato Centrale",   "type": "Market",   "distance": 3.00, "distanceUnit": "km" }
  ]
}
```

---

## Domain Model Design Notes

**`Property` is immutable** — constructed via a builder, no setters. All child lists (`images`, `amenities`, `nearbyPlaces`) are wrapped with `Collections.unmodifiableList`.

**`PropertySearchCriteria`** is a value object record. All fields are nullable — any null field means "no filter on this dimension". The `PropertySpecification` builds a JPA `Predicate` list at runtime and always appends `status = AVAILABLE`.

**Separation of persistence and domain** — the four JPA entities live only in `infrastructure/persistence/entity`. The `PropertyMapper` translates between them and the domain model. The domain `Property` class has zero Spring/JPA annotations.

---

## Seeded Properties

| Ref | Title | Type | Listing | Price | City |
|---|---|---|---|---|---|
| PROP-1001 | Modern 2BR Apartment in City Centre | APARTMENT | SALE | €185,000 | Tirana, Albania |
| PROP-1002 | Luxury Villa with Pool – Tuscany | VILLA | RENT | €4,500/mo | Florence, Italy |
| PROP-1003 | Prime Office Space – Central London | OFFICE | SALE | £1,250,000 | London, UK |
