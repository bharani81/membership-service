package com.membership.strategy;

import java.math.BigDecimal;

/**
 * Immutable value object carrying the user's current metrics for tier evaluation.
 *
 * <p>Using a Java record ensures this context object is thread-safe and cannot
 * be accidentally mutated as it passes through the strategy chain.
 *
 * @param userId       the user being evaluated
 * @param orderCount   total number of orders placed by the user
 * @param monthlySpend the user's average or current monthly spend
 * @param cohort       optional user cohort identifier (e.g., "VIP", "STUDENT")
 */
public record TierEvaluationContext(
        Long userId,
        int orderCount,
        BigDecimal monthlySpend,
        String cohort
) {
    public TierEvaluationContext {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        if (monthlySpend == null) {
            monthlySpend = BigDecimal.ZERO;
        }
    }
}
