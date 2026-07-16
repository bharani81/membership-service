-- V6: Seed reference data - Plans, Tiers, and Tier Rules

-- ============================================================
-- Membership Plans
-- ============================================================
INSERT INTO membership_plans (name, duration_months, price, active) VALUES
    ('Monthly',   1,  99.00, TRUE),
    ('Quarterly', 3,  249.00, TRUE),
    ('Yearly',    12, 799.00, TRUE);

-- ============================================================
-- Membership Tiers (priority: higher = better tier)
-- ============================================================
INSERT INTO membership_tiers (name, priority, discount_percentage, free_delivery, priority_support, early_access, configuration, active)
VALUES
    ('Silver',
     1,
     5.00,
     FALSE,
     FALSE,
     FALSE,
     '{"cashbackPercent": 1, "maxItemsPerOrder": 10, "dedicatedAccountManager": false}'::jsonb,
     TRUE),

    ('Gold',
     2,
     10.00,
     TRUE,
     FALSE,
     FALSE,
     '{"cashbackPercent": 3, "maxItemsPerOrder": 25, "dedicatedAccountManager": false, "loungeAccess": false}'::jsonb,
     TRUE),

    ('Platinum',
     3,
     20.00,
     TRUE,
     TRUE,
     TRUE,
     '{"cashbackPercent": 5, "maxItemsPerOrder": 100, "dedicatedAccountManager": true, "loungeAccess": true, "conciergeSupport": true}'::jsonb,
     TRUE);

-- ============================================================
-- Tier Rules
-- Composite: ALL rules for a tier must pass for eligibility
-- ============================================================

-- Silver: >= 5 orders OR >= 500 monthly spend
INSERT INTO tier_rules (tier_id, minimum_orders, minimum_monthly_spend, cohort, rule_priority)
SELECT id, 5, NULL, NULL, 10 FROM membership_tiers WHERE name = 'Silver';

INSERT INTO tier_rules (tier_id, minimum_orders, minimum_monthly_spend, cohort, rule_priority)
SELECT id, NULL, 500.00, NULL, 5 FROM membership_tiers WHERE name = 'Silver';

-- Gold: >= 20 orders AND >= 2000 monthly spend
INSERT INTO tier_rules (tier_id, minimum_orders, minimum_monthly_spend, cohort, rule_priority)
SELECT id, 20, 2000.00, NULL, 10 FROM membership_tiers WHERE name = 'Gold';

-- Platinum: >= 50 orders AND >= 5000 monthly spend, OR VIP cohort
INSERT INTO tier_rules (tier_id, minimum_orders, minimum_monthly_spend, cohort, rule_priority)
SELECT id, 50, 5000.00, NULL, 20 FROM membership_tiers WHERE name = 'Platinum';

INSERT INTO tier_rules (tier_id, minimum_orders, minimum_monthly_spend, cohort, rule_priority)
SELECT id, NULL, NULL, 'VIP', 30 FROM membership_tiers WHERE name = 'Platinum';
