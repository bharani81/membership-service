# Database Schema Documentation

## Overview

All DDL is managed exclusively by Flyway. Hibernate is configured with `ddl-auto: validate` — it validates the schema at startup but never modifies it.

---

## Tables

### `membership_plans`
Stores purchasable subscription plans.

| Column | Type | Constraints | Description |
|---|---|---|---|
| id | BIGSERIAL | PK | Auto-generated |
| name | VARCHAR(100) | NOT NULL, UNIQUE | "Monthly", "Quarterly", "Yearly" |
| duration_months | INT | NOT NULL, CHECK > 0 | Validity period |
| price | NUMERIC(10,2) | NOT NULL, CHECK >= 0 | Subscription cost |
| active | BOOLEAN | NOT NULL DEFAULT TRUE | Soft delete |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | Audit |

**Indexes:** `idx_plans_active (active)`

---

### `membership_tiers`
Defines tier levels and their benefits.

| Column | Type | Constraints | Description |
|---|---|---|---|
| id | BIGSERIAL | PK | Auto-generated |
| name | VARCHAR(100) | NOT NULL, UNIQUE | "Silver", "Gold", "Platinum" |
| priority | INT | NOT NULL | Higher = better tier. Used for upgrade/downgrade validation |
| discount_percentage | NUMERIC(5,2) | DEFAULT 0, CHECK 0-100 | Discount offered |
| free_delivery | BOOLEAN | NOT NULL DEFAULT FALSE | |
| priority_support | BOOLEAN | NOT NULL DEFAULT FALSE | |
| early_access | BOOLEAN | NOT NULL DEFAULT FALSE | |
| configuration | JSONB | | Extensible benefit blob |
| active | BOOLEAN | NOT NULL DEFAULT TRUE | Soft delete |

**Indexes:** `idx_tiers_active_priority (active, priority DESC)`

**Note on `configuration`:** New benefits (e.g., `loungeAccess`, `conciergeSupport`) are added as JSONB keys without schema changes. This is the key extensibility mechanism.

---

### `tier_rules`
Data-driven eligibility rules. No code changes required for new cohorts.

| Column | Type | Constraints | Description |
|---|---|---|---|
| id | BIGSERIAL | PK | |
| tier_id | BIGINT | FK → membership_tiers, CASCADE DELETE | |
| minimum_orders | INT | NULLABLE | Null = not evaluated |
| minimum_monthly_spend | NUMERIC(10,2) | NULLABLE | Null = not evaluated |
| cohort | VARCHAR(100) | NULLABLE | "VIP", "STUDENT", "EMPLOYEE", etc. Null = not evaluated |
| rule_priority | INT | NOT NULL DEFAULT 0 | Higher = evaluated first |

**Indexes:** `idx_tier_rules_tier`, `idx_tier_rules_priority`

**Rule semantics:**
- Multiple rules per tier = OR logic (any passing rule qualifies)
- Within one rule, all non-null dimensions must pass (AND logic)

---

### `user_memberships`
Core entity tracking user subscription state.

| Column | Type | Constraints | Description |
|---|---|---|---|
| id | BIGSERIAL | PK | |
| user_id | BIGINT | NOT NULL | External user identifier |
| plan_id | BIGINT | FK → membership_plans | |
| tier_id | BIGINT | FK → membership_tiers | |
| status | VARCHAR(20) | CHECK IN ('ACTIVE','EXPIRED','CANCELLED','PENDING') | |
| start_date | DATE | NOT NULL | |
| expiry_date | DATE | NOT NULL, CHECK > start_date | |
| version | BIGINT | NOT NULL DEFAULT 0 | Optimistic locking |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |
| updated_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |

**Indexes:**
- `idx_memberships_one_active_per_user` — **partial unique index** on `user_id WHERE status='ACTIVE'`. Enforces one active membership per user at the database level.
- `idx_memberships_expiry_status (expiry_date, status)` — optimizes the daily expiry scheduler query
- `idx_memberships_user_id` — speeds up user membership lookups

---

### `membership_history`
Append-only audit log. No UPDATE or DELETE ever.

| Column | Type | Constraints | Description |
|---|---|---|---|
| id | BIGSERIAL | PK | |
| membership_id | BIGINT | FK → user_memberships | |
| user_id | BIGINT | NOT NULL | Denormalized for query convenience |
| action | VARCHAR(20) | CHECK IN ('SUBSCRIBE','UPGRADE','DOWNGRADE','CANCEL','RENEW','EXPIRE') | |
| from_plan_id | BIGINT | FK NULLABLE | |
| to_plan_id | BIGINT | FK NULLABLE | |
| from_tier_id | BIGINT | FK NULLABLE | |
| to_tier_id | BIGINT | FK NULLABLE | |
| performed_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |
| metadata | JSONB | NULLABLE | Arbitrary audit context |

**Indexes:** `idx_history_membership_id`, `idx_history_user_id`, `idx_history_performed_at DESC`

---

## ERD (Simplified)

```
membership_plans          membership_tiers
      │                         │
      │ plan_id                 │ tier_id
      ▼                         ▼
            user_memberships
                  │
                  │ membership_id
                  ▼
           membership_history

membership_tiers
      │ tier_id
      ▼
   tier_rules
```

---

## Migration History

| Version | Description |
|---|---|
| V1 | Create `membership_plans` table |
| V2 | Create `membership_tiers` table with JSONB benefits |
| V3 | Create `tier_rules` table |
| V4 | Create `user_memberships` table with optimistic locking |
| V5 | Create `membership_history` append-only audit table |
| V6 | Seed Silver/Gold/Platinum tiers, Monthly/Quarterly/Yearly plans, tier rules |
