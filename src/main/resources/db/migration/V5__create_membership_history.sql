-- V5: Immutable membership history / audit log
CREATE TABLE membership_history (
    id             BIGSERIAL    PRIMARY KEY,
    membership_id  BIGINT       NOT NULL REFERENCES user_memberships (id),
    user_id        BIGINT       NOT NULL,
    action         VARCHAR(20)  NOT NULL CHECK (action IN ('SUBSCRIBE','UPGRADE','DOWNGRADE','CANCEL','RENEW','EXPIRE')),
    from_plan_id   BIGINT       REFERENCES membership_plans (id),
    to_plan_id     BIGINT       REFERENCES membership_plans (id),
    from_tier_id   BIGINT       REFERENCES membership_tiers (id),
    to_tier_id     BIGINT       REFERENCES membership_tiers (id),
    performed_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    metadata       JSONB
);

CREATE INDEX idx_history_membership_id ON membership_history (membership_id);
CREATE INDEX idx_history_user_id ON membership_history (user_id);
CREATE INDEX idx_history_performed_at ON membership_history (performed_at DESC);

COMMENT ON TABLE membership_history IS 'Append-only audit log. Records every state transition for compliance and debugging.';
COMMENT ON COLUMN membership_history.metadata IS 'Arbitrary context: reason codes, actor info, system metadata';
