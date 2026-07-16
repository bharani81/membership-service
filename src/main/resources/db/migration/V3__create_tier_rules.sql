-- V3: Tier eligibility rules (data-driven, no code changes needed for new cohorts/rules)
CREATE TABLE tier_rules (
    id                    BIGSERIAL PRIMARY KEY,
    tier_id               BIGINT         NOT NULL REFERENCES membership_tiers (id) ON DELETE CASCADE,
    minimum_orders        INT            CHECK (minimum_orders >= 0),
    minimum_monthly_spend NUMERIC(10, 2) CHECK (minimum_monthly_spend >= 0),
    cohort                VARCHAR(100),
    rule_priority         INT            NOT NULL DEFAULT 0
);

CREATE INDEX idx_tier_rules_tier ON tier_rules (tier_id);
CREATE INDEX idx_tier_rules_priority ON tier_rules (rule_priority DESC);

COMMENT ON TABLE tier_rules IS 'Data-driven eligibility rules for each tier. New cohorts/rules require no code changes.';
COMMENT ON COLUMN tier_rules.cohort IS 'User cohort identifier: STUDENT, EMPLOYEE, VIP, REFERRAL, etc.';
COMMENT ON COLUMN tier_rules.rule_priority IS 'Evaluation order within a tier - higher = evaluated first';
