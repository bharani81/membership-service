package com.membership.strategy;

import com.membership.entity.TierRule;

/**
 * Strategy interface for evaluating a single tier eligibility rule.
 *
 * <p>Each implementation handles one type of eligibility criterion
 * (e.g., order count, monthly spend, user cohort). New criteria can be
 * added by creating a new {@code @Component} implementing this interface—
 * no changes to the evaluation engine are required.
 *
 * <p>Implementations are auto-discovered by the {@link TierEvaluationEngine}
 * via Spring's dependency injection, keyed by {@link #strategyType()}.
 */
public interface TierEvaluationStrategy {

    /**
     * Evaluates whether the given context satisfies the provided rule.
     *
     * @param context the user's current metrics (orders, spend, cohort)
     * @param rule    the rule to evaluate against
     * @return {@code true} if the rule is satisfied, {@code false} otherwise
     */
    boolean evaluate(TierEvaluationContext context, TierRule rule);

    /**
     * A stable string identifier for this strategy type.
     * Used to match strategies to rules without hardcoded conditionals.
     *
     * @return a non-null, non-blank strategy type key
     */
    String strategyType();
}
