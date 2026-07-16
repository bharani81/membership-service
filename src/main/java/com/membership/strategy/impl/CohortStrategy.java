package com.membership.strategy.impl;

import com.membership.entity.TierRule;
import com.membership.strategy.TierEvaluationContext;
import com.membership.strategy.TierEvaluationStrategy;
import org.springframework.stereotype.Component;

/**
 * Evaluates tier eligibility based on the user's cohort (e.g., VIP, STUDENT, EMPLOYEE).
 *
 * <p>A rule is satisfied if:
 * <ul>
 *   <li>The rule specifies no cohort ({@code cohort} is null or blank), OR</li>
 *   <li>The user's cohort matches the rule's cohort (case-insensitive).</li>
 * </ul>
 *
 * <p>New cohorts (e.g., REFERRAL, FESTIVAL) can be added purely via database inserts.
 * No code changes required.
 */
@Component
public class CohortStrategy implements TierEvaluationStrategy {

    public static final String TYPE = "COHORT";

    @Override
    public boolean evaluate(TierEvaluationContext context, TierRule rule) {
        if (rule.getCohort() == null || rule.getCohort().isBlank()) {
            return true; // This rule dimension is not applicable
        }
        if (context.cohort() == null || context.cohort().isBlank()) {
            return false; // Rule requires a cohort but user has none
        }
        return rule.getCohort().equalsIgnoreCase(context.cohort());
    }

    @Override
    public String strategyType() {
        return TYPE;
    }
}
