# Membership Service

> A production-ready Spring Boot microservice for managing user membership subscriptions, built to demonstrate clean architecture, design patterns, concurrency safety, and operational readiness.

---

## Overview

This service handles the full membership lifecycle:
- **Subscribe** users to monthly, quarterly, or yearly plans
- **Upgrade / Downgrade** between Silver, Gold, and Platinum tiers
- **Cancel** memberships with audit history
- **Evaluate** tier eligibility via a pluggable Strategy Pattern engine
- **Auto-expire** memberships via a scheduled job
- **Handle concurrent modifications** safely via optimistic locking

---

## Architecture

```
Controller → Application Service → Domain Service → Repository → PostgreSQL
```

Design patterns implemented:
- **Strategy Pattern** — Tier evaluation engine (extensible, data-driven)
- **Factory Pattern** — Membership action routing (subscribe/upgrade/downgrade/cancel)
- **Specification Pattern** — Composable JPA plan filtering
- **Builder Pattern** — All DTOs as immutable records/builders

See [`docs/architecture.md`](docs/architecture.md) for full details.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| Database | PostgreSQL 16 |
| Migrations | Flyway |
| ORM | Spring Data JPA / Hibernate |
| Mapping | MapStruct |
| Utilities | Lombok |
| Validation | Bean Validation (Jakarta) |
| API Docs | SpringDoc OpenAPI / Swagger UI |
| Containerization | Docker + Docker Compose |
| Testing | JUnit 5 + Mockito + Testcontainers |
| CI | GitHub Actions |
| Logging | SLF4J + Logback |
| Build | Maven 3.9 |

---

## Design Decisions

### Why Flyway and not `ddl-auto: create`?
Flyway gives DDL ownership to the operations team. Hibernate is configured to `validate` only—it cannot modify the schema. This prevents accidental data loss in production.

### Why Testcontainers and not H2?
H2's in-memory dialect differs from PostgreSQL in subtle ways (e.g., partial indexes, JSONB, constraint semantics). Testcontainers provides full production parity.

### Why Optimistic Locking?
The membership update endpoints are low-contention but not zero-contention. Pessimistic locking would serialize all requests unnecessarily. The `@Version` field on `UserMembership` handles the rare concurrent-modification case gracefully with a `409 Conflict`.

### Why Strategy Pattern for tier evaluation?
Tier eligibility rules change frequently—new cohorts (STUDENT, VIP), seasonal rules (FESTIVAL), and ML-based recommendations are all expected. The strategy engine makes each new rule a single `@Component` class. No changes to the evaluator itself.

### Why Factory Pattern for actions?
Membership actions (subscribe, upgrade, cancel) have distinct business rules. The factory maps `MembershipAction` → handler without switch/if chains, making each action independently testable and the set of actions extensible.

---

## Folder Structure

```
membership-service/
├── .github/workflows/ci.yml        # GitHub Actions CI pipeline
├── docs/
│   ├── architecture.md
│   ├── api-design.md
│   └── db-schema.md
├── src/
│   ├── main/java/com/membership/
│   │   ├── config/                 # OpenAPI, Jackson config
│   │   ├── controller/             # REST controllers (3)
│   │   ├── dto/
│   │   │   ├── request/            # Validated request DTOs
│   │   │   └── response/           # Response DTOs
│   │   ├── entity/                 # JPA entities + enums + converters
│   │   ├── exception/              # Exception hierarchy + global handler
│   │   ├── factory/                # Factory pattern (action handlers)
│   │   ├── mapper/                 # MapStruct mappers
│   │   ├── repository/             # Spring Data repositories + Specifications
│   │   ├── scheduler/              # Expiry scheduler
│   │   ├── service/                # Service interfaces + implementations
│   │   └── strategy/               # Strategy pattern (tier evaluation)
│   └── main/resources/
│       ├── db/migration/           # V1–V6 Flyway migrations
│       ├── application.yml
│       └── application-docker.yml
├── docker-compose.yml
├── Dockerfile
└── pom.xml
```

---

## How to Run

### Prerequisites
- Docker and Docker Compose installed

### One-Command Startup
```bash
git clone <repo-url>
cd membership-service
docker compose up --build
```

This will:
1. Start PostgreSQL 16
2. Build the application (multi-stage Docker)
3. Apply Flyway migrations V1–V6 (including seed data)
4. Start the service on port 8080

### Access Swagger UI
```
http://localhost:8080/swagger-ui.html
```

### Verify the service
```bash
# List plans
curl http://localhost:8080/api/v1/plans

# List tiers
curl http://localhost:8080/api/v1/tiers

# Subscribe
curl -X POST http://localhost:8080/api/v1/memberships \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "planId": 1, "tierId": 1}'

# Evaluate tier
curl -X POST http://localhost:8080/api/v1/users/1/tier/evaluate \
  -H "Content-Type: application/json" \
  -d '{"orderCount": 25, "monthlySpend": 2500.00}'
```

### Local Development (without Docker)
Requires a local PostgreSQL instance:
```bash
createdb membership_db
psql -c "CREATE USER membership_user WITH PASSWORD 'membership_pass';"
psql -c "GRANT ALL ON DATABASE membership_db TO membership_user;"

mvn spring-boot:run
```

---

## API Documentation

Full API reference: [`docs/api-design.md`](docs/api-design.md)

Interactive Swagger UI: `http://localhost:8080/swagger-ui.html`

### Quick Reference

| Method | Path | Description |
|---|---|---|
| GET | /api/v1/plans | List all active plans |
| GET | /api/v1/plans/{id} | Get plan by ID |
| GET | /api/v1/tiers | List all active tiers |
| POST | /api/v1/memberships | Subscribe |
| PUT | /api/v1/memberships/{id}/upgrade | Upgrade |
| PUT | /api/v1/memberships/{id}/downgrade | Downgrade |
| PUT | /api/v1/memberships/{id}/cancel | Cancel |
| GET | /api/v1/users/{id}/membership | Current membership |
| GET | /api/v1/users/{id}/membership/status | Expiry status |
| POST | /api/v1/users/{id}/tier/evaluate | Evaluate tier eligibility |

---

## Testing

```bash
# All tests (unit + integration via Testcontainers)
mvn clean test

# Only unit tests (fast)
mvn test -Dtest="*ServiceTest,*StrategyTest"

# Only Testcontainers tests (requires Docker)
mvn test -Dtest="*RepositoryTest"

# Full build with static analysis
mvn clean verify
```

### Test Coverage

| Layer | Type | Coverage |
|---|---|---|
| Service | Mockito unit tests | Subscribe, upgrade, downgrade, cancel, not-found, expiry status |
| Strategy engine | Mockito unit tests | All three strategies, composite evaluation, VIP cohort bypass |
| Repository | Testcontainers integration | Active lookup, expiry query, version increment |
| Controller | MockMvc | 201, 400, 404, 409 scenarios |

---

## Concurrency Demo

To reproduce the optimistic locking `409` behavior:

```bash
# Subscribe a user
MEMBERSHIP_ID=$(curl -s -X POST http://localhost:8080/api/v1/memberships \
  -H "Content-Type: application/json" \
  -d '{"userId": 99, "planId": 2, "tierId": 1}' | jq .id)

# Concurrent upgrade + cancel (one will win, one will get 409)
curl -X PUT http://localhost:8080/api/v1/memberships/$MEMBERSHIP_ID/upgrade \
  -H "Content-Type: application/json" \
  -d '{"targetPlanId": 3, "targetTierId": 2}' &

curl -X PUT http://localhost:8080/api/v1/memberships/$MEMBERSHIP_ID/cancel &

wait
```

---

## Future Improvements

| Feature | Description |
|---|---|
| **Redis Caching** | Cache active membership lookups to reduce DB load on high-traffic GET endpoints |
| **Kafka Events** | Publish `MembershipCreated`, `MembershipUpgraded` domain events for downstream consumers (notifications, analytics) |
| **CQRS** | Separate read and write models — ReadModel serves GET requests from a projected view |
| **Outbox Pattern** | Guarantee event delivery by writing events to an outbox table in the same transaction |
| **Event Sourcing** | Replace mutable state with an event stream — audit history becomes the source of truth |
| **Multi-region Deployment** | Geo-replicated Postgres with conflict resolution for global scale |
| **Micrometer + Prometheus** | Expose `/actuator/prometheus` with custom counters for subscription rate, tier distribution, expiry rate |
| **OpenTelemetry** | Distributed tracing with trace propagation across service boundaries |
| **OAuth2 / JWT** | Secure all endpoints with Spring Security, validate JWT claims against userId path parameter |
| **RBAC** | Role-based access: `ADMIN` can manage plans/tiers, `USER` can only manage own membership |
| **Rate Limiting** | Bucket4j or Redis-based rate limiting on subscription endpoints to prevent abuse |
| **AI Tier Recommendation** | `AIRecommendationStrategy` that calls an ML model for personalized tier suggestions |
