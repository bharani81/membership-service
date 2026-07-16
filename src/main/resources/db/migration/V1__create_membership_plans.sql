-- V1: Membership Plans
CREATE TABLE membership_plans (
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(100)   NOT NULL UNIQUE,
    duration_months INT           NOT NULL CHECK (duration_months > 0),
    price          NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    active         BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_plans_active ON membership_plans (active);

COMMENT ON TABLE membership_plans IS 'Available membership subscription plans';
COMMENT ON COLUMN membership_plans.duration_months IS 'Validity period in months (1=Monthly, 3=Quarterly, 12=Yearly)';
