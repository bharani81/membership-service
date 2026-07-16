package com.membership.strategy.impl;

import com.membership.entity.TierRule;
import com.membership.strategy.TierEvaluationContext;
import com.membership.strategy.TierEvaluationStrategy;
import org.springframework.stereotype.Component;

/**
 * Evaluates tier eligibility based on the user's monthly spend.
 *
 * <p>A rule is considered satisfied if:
 * <ul>
 *   <li>The rule has no spend threshold ({@code minimumMonthlySpend} is null), OR</li>
 *   <li>The user's monthly spend meets or exceeds the threshold.</li>
 * </ul>
 */
@Component
public class MonthlySpendStrategy implements TierEvaluationStrategy {

    public static final String TYPE = "MONTHLY_SPEND";

    @Override
    public boolean evaluate(TierEvaluationContext context, TierRule rule) {
        if (rule.getMinimumMonthlySpend() == null) {
            return true; // This rule dimension is not applicable
        }
        return context.monthlySpend().compareTo(rule.getMinimumMonthlySpend()) >= 0;
    }

    @Override
    public String strategyType() {
        return TYPE;
    }
}
