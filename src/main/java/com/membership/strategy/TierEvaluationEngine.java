package com.membership.strategy;

import com.membership.entity.MembershipTier;
import com.membership.entity.TierRule;
import com.membership.repository.MembershipTierRepository;
import com.membership.repository.TierRuleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Composite tier evaluation engine that coordinates all {@link TierEvaluationStrategy} implementations.
 *
 * <p><strong>Algorithm:</strong>
 * <ol>
 *   <li>Fetch all active tiers ordered by priority (highest first).</li>
 *   <li>For each tier, fetch its rules.</li>
 *   <li>For each rule, apply ALL three strategies (order count, spend, cohort).</li>
 *   <li>A rule is satisfied only if ALL applicable strategy dimensions pass.</li>
 *   <li>A tier is awarded if ANY of its rules is fully satisfied.</li>
 *   <li>Return the highest-priority tier the user qualifies for.</li>
 * </ol>
 *
 * <p><strong>Extensibility:</strong> Adding a new strategy (e.g., {@code FestivalStrategy})
 * only requires creating a new {@code @Component} implementing {@link TierEvaluationStrategy}.
 * This engine auto-discovers it via Spring injection. Zero changes here.
 */
@Slf4j
@Component
public class TierEvaluationEngine {

    private final List<TierEvaluationStrategy> strategies;
    private final MembershipTierRepository tierRepository;
    private final TierRuleRepository tierRuleRepository;

    public TierEvaluationEngine(List<TierEvaluationStrategy> strategies,
                                MembershipTierRepository tierRepository,
                                TierRuleRepository tierRuleRepository) {
        this.strategies = strategies;
        this.tierRepository = tierRepository;
        this.tierRuleRepository = tierRuleRepository;
        log.info("TierEvaluationEngine initialized with {} strategies: {}",
                strategies.size(),
                strategies.stream().map(TierEvaluationStrategy::strategyType).collect(Collectors.joining(", ")));
    }

    /**
     * Evaluates the best tier a user qualifies for given their current metrics.
     *
     * @param context the user's evaluation context
     * @return the highest-priority matching tier, or empty if no tier qualifies
     */
    public Optional<MembershipTier> evaluate(TierEvaluationContext context) {
        log.info("Evaluating tier for userId={} orderCount={} monthlySpend={} cohort={}",
                context.userId(), context.orderCount(), context.monthlySpend(), context.cohort());

        List<MembershipTier> tiersDescending = tierRepository.findAllByActiveTrueOrderByPriorityDesc();

        for (MembershipTier tier : tiersDescending) {
            List<TierRule> rules = tierRuleRepository.findByTierIdOrderByRulePriorityDesc(tier.getId());

            if (rules.isEmpty()) {
                log.debug("Tier {} has no rules, skipping", tier.getName());
                continue;
            }

            if (anyRuleSatisfied(context, rules)) {
                log.info("User userId={} qualifies for tier={}", context.userId(), tier.getName());
                return Optional.of(tier);
            }
        }

        log.info("User userId={} does not qualify for any tier", context.userId());
        return Optional.empty();
    }

    /**
     * Returns true if at least one rule in the list is fully satisfied by the context.
     * Each rule is evaluated as AND across all active strategy dimensions.
     */
    private boolean anyRuleSatisfied(TierEvaluationContext context, List<TierRule> rules) {
        return rules.stream().anyMatch(rule -> allStrategiesPass(context, rule));
    }

    /**
     * A rule passes only if every strategy considers it satisfied.
     * Strategies with inapplicable dimensions return true by convention.
     */
    private boolean allStrategiesPass(TierEvaluationContext context, TierRule rule) {
        return strategies.stream().allMatch(strategy -> strategy.evaluate(context, rule));
    }
}
