package com.membership.strategy.impl;

import com.membership.entity.TierRule;
import com.membership.strategy.TierEvaluationContext;
import com.membership.strategy.TierEvaluationStrategy;
import org.springframework.stereotype.Component;

/**
 * Evaluates tier eligibility based on the user's total order count.
 *
 * <p>A rule is considered satisfied if:
 * <ul>
 *   <li>The rule has no minimum order requirement ({@code minimumOrders} is null), OR</li>
 *   <li>The user's order count meets or exceeds the threshold.</li>
 * </ul>
 */
@Component
public class OrderCountStrategy implements TierEvaluationStrategy {

    public static final String TYPE = "ORDER_COUNT";

    @Override
    public boolean evaluate(TierEvaluationContext context, TierRule rule) {
        if (rule.getMinimumOrders() == null) {
            return true; // This rule dimension is not applicable
        }
        return context.orderCount() >= rule.getMinimumOrders();
    }

    @Override
    public String strategyType() {
        return TYPE;
    }
}
