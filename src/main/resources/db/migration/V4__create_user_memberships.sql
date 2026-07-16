-- V4: User memberships with optimistic locking version column
CREATE TABLE user_memberships (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    plan_id     BIGINT       NOT NULL REFERENCES membership_plans (id),
    tier_id     BIGINT       NOT NULL REFERENCES membership_tiers (id),
    status      VARCHAR(20)  NOT NULL CHECK (status IN ('ACTIVE', 'EXPIRED', 'CANCELLED', 'PENDING')),
    start_date  DATE         NOT NULL,
    expiry_date DATE         NOT NULL CHECK (expiry_date > start_date),
    version     BIGINT       NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Enforce only one ACTIVE membership per user at a time
CREATE UNIQUE INDEX idx_memberships_one_active_per_user
    ON user_memberships (user_id)
    WHERE status = 'ACTIVE';

CREATE INDEX idx_memberships_user_id ON user_memberships (user_id);
CREATE INDEX idx_memberships_expiry_status ON user_memberships (expiry_date, status);
CREATE INDEX idx_memberships_status ON user_memberships (status);

COMMENT ON TABLE user_memberships IS 'Active and historical user membership records';
COMMENT ON COLUMN user_memberships.version IS 'Hibernate optimistic locking version - prevents concurrent overwrites';
COMMENT ON COLUMN user_memberships.status IS 'ACTIVE | EXPIRED | CANCELLED | PENDING';
