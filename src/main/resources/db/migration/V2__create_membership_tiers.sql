-- V2: Membership Tiers with configurable JSONB benefits
CREATE TABLE membership_tiers (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(100)   NOT NULL UNIQUE,
    priority            INT            NOT NULL,
    discount_percentage NUMERIC(5, 2)  NOT NULL DEFAULT 0 CHECK (discount_percentage BETWEEN 0 AND 100),
    free_delivery       BOOLEAN        NOT NULL DEFAULT FALSE,
    priority_support    BOOLEAN        NOT NULL DEFAULT FALSE,
    early_access        BOOLEAN        NOT NULL DEFAULT FALSE,
    configuration       JSONB,
    active              BOOLEAN        NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_tiers_active_priority ON membership_tiers (active, priority DESC);

COMMENT ON TABLE membership_tiers IS 'Membership tier definitions (Silver, Gold, Platinum)';
COMMENT ON COLUMN membership_tiers.configuration IS 'Extensible JSONB blob for future tier benefit attributes';
COMMENT ON COLUMN membership_tiers.priority IS 'Higher priority = higher tier. Used for upgrade/downgrade validation';
