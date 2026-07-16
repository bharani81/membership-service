# Architecture Documentation

## Overview

The Membership Service is a production-ready Spring Boot application built around Clean Architecture principles.
Every layer has a single responsibility, and dependencies flow strictly inward.

---

## Layered Architecture

```
┌─────────────────────────────────┐
│         HTTP Client             │
└──────────────┬──────────────────┘
               │ REST
               ▼
┌─────────────────────────────────┐
│       Controller Layer          │  ← @RestController
│   MembershipPlanController      │  ← @Valid, @PathVariable
│   MembershipTierController      │  ← OpenAPI @Operation
│   UserMembershipController      │
└──────────────┬──────────────────┘
               │ DTO only
               ▼
┌─────────────────────────────────┐
│     Application Service Layer   │  ← @Service, @Transactional
│   UserMembershipServiceImpl     │  ← Orchestration
│   MembershipPlanServiceImpl     │  ← Business flow
│   TierEvaluationServiceImpl     │  ← Thin delegation
└──────────────┬──────────────────┘
               │
       ┌───────┴──────────┐
       ▼                  ▼
┌──────────────┐  ┌──────────────────┐
│Domain Layer  │  │  Factory Pattern │
│              │  │  MembershipAction│
│  Strategy    │  │  Factory         │
│  Pattern     │  │  ├─ Subscribe    │
│  ├─ Order    │  │  ├─ Upgrade      │
│  ├─ Spend    │  │  ├─ Downgrade    │
│  ├─ Cohort   │  │  └─ Cancel       │
│  └─ Engine   │  └──────┬───────────┘
└──────┬───────┘         │
       └────────┬────────┘
                ▼
┌─────────────────────────────────┐
│       Repository Layer          │  ← Spring Data JPA
│   UserMembershipRepository      │  ← JOIN FETCH queries
│   MembershipPlanRepository      │  ← JpaSpecificationExecutor
│   MembershipTierRepository      │
│   MembershipHistoryRepository   │  ← Append-only
│   TierRuleRepository            │
└──────────────┬──────────────────┘
               │ JDBC
               ▼
┌─────────────────────────────────┐
│         PostgreSQL              │  ← Flyway-managed schema
│   membership_plans              │  ← V1
│   membership_tiers              │  ← V2
│   tier_rules                    │  ← V3
│   user_memberships              │  ← V4 (with @Version)
│   membership_history            │  ← V5 (append-only)
└─────────────────────────────────┘
```

---

## Design Patterns

### Strategy Pattern — Tier Evaluation Engine

The tier evaluation system is fully data-driven and extensible:

```
TierEvaluationEngine
    │
    ├─ List<TierEvaluationStrategy> (Spring-injected)
    │      ├─ OrderCountStrategy
    │      ├─ MonthlySpendStrategy
    │      └─ CohortStrategy
    │
    └─ Algorithm:
         1. Fetch tiers ordered by priority DESC
         2. For each tier, fetch its rules
         3. A rule passes if ALL strategies return true
         4. A tier qualifies if ANY rule passes
         5. Return first (highest priority) qualifying tier
```

**Extensibility:** Adding `FestivalStrategy` requires only:
- Create `@Component class FestivalStrategy implements TierEvaluationStrategy`
- Engine auto-discovers it via Spring injection
- Zero changes to existing code

### Factory Pattern — Membership Actions

```
MembershipActionFactory
    │
    ├─ Map<MembershipAction, MembershipActionHandler>
    │      ├─ SUBSCRIBE → SubscribeActionHandler
    │      ├─ UPGRADE   → UpgradeActionHandler
    │      ├─ DOWNGRADE → DowngradeActionHandler
    │      └─ CANCEL    → CancelActionHandler
    │
    └─ getHandler(action).handle(context)
```

No switch/if chains. O(1) dispatch. Adding RENEW requires one new `@Component`.

### Specification Pattern

`MembershipPlanSpecification` provides composable JPA predicates:
```java
Specification<MembershipPlan> spec = isActive()
    .and(hasPriceLessThanOrEqual(new BigDecimal("500")));
planRepository.findAll(spec);
```

---

## Concurrency Safety

`UserMembership` has `@Version Long version` managed by Hibernate.

**Scenario:**
1. Request A reads membership v=0, intends to upgrade
2. Request B reads same membership v=0, intends to cancel
3. Request A commits → version becomes v=1
4. Request B commits → detects v≠0, throws `ObjectOptimisticLockingFailureException`
5. `GlobalExceptionHandler` catches it → returns `HTTP 409 Conflict`

The caller is expected to retry. A `@RetryOnConflict` AOP aspect can be layered on
without changing business logic.

---

## Transaction Strategy

- **Read operations:** `@Transactional(readOnly = true)` — enables Hibernate read-only optimization, avoids dirty checking overhead
- **Write operations:** `@Transactional` — full ACID guarantees
- **Controllers:** Never `@Transactional` — transaction boundary is exclusively in the service layer
- **History writes:** Always within the same transaction as the state change — no orphaned records

---

## Audit Trail

`membership_history` is append-only. The JPA entity marks all columns `updatable = false`.
Every state transition records:
- What changed (from/to plan and tier)
- When it happened
- Who initiated it (userId)
- Why (via optional JSONB metadata)

---

## Technology Decisions

| Decision | Choice | Rationale |
|---|---|---|
| Database migrations | Flyway | DDL ownership belongs to operations, not Hibernate |
| `ddl-auto` | `validate` | Hibernate only validates schema, never modifies it |
| Testing DB | Testcontainers | Production parity — H2 masks Postgres-specific behavior |
| Mapping | MapStruct | Compile-time, zero reflection, type-safe |
| Locking | Optimistic (`@Version`) | Lower contention than pessimistic; appropriate for membership scenario |
| `open-in-view` | `false` | Prevents lazy-loading within the view layer — forces explicit fetch strategy |
| Batch size | 25 | Reduces N+1 impact on collections |
