package com.membership.strategy;

import com.membership.entity.MembershipTier;
import com.membership.entity.TierRule;
import com.membership.repository.MembershipTierRepository;
import com.membership.repository.TierRuleRepository;
import com.membership.strategy.impl.CohortStrategy;
import com.membership.strategy.impl.MonthlySpendStrategy;
import com.membership.strategy.impl.OrderCountStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TierEvaluationEngine Unit Tests")
class TierEvaluationEngineTest {

    @Mock private MembershipTierRepository tierRepository;
    @Mock private TierRuleRepository tierRuleRepository;

    private TierEvaluationEngine engine;
    private MembershipTier silverTier;
    private MembershipTier goldTier;
    private MembershipTier platinumTier;

    @BeforeEach
    void setUp() {
        List<TierEvaluationStrategy> strategies = List.of(
                new OrderCountStrategy(),
                new MonthlySpendStrategy(),
                new CohortStrategy()
        );
        engine = new TierEvaluationEngine(strategies, tierRepository, tierRuleRepository);

        silverTier = MembershipTier.builder().id(1L).name("Silver").priority(1).active(true).build();
        goldTier = MembershipTier.builder().id(2L).name("Gold").priority(2).active(true).build();
        platinumTier = MembershipTier.builder().id(3L).name("Platinum").priority(3).active(true).build();
    }

    @Test
    @DisplayName("Should recommend Platinum when order count and spend qualify")
    void evaluate_recommendsPlatinum() {
        TierRule platinumRule = TierRule.builder()
                .tier(platinumTier)
                .minimumOrders(50)
                .minimumMonthlySpend(new BigDecimal("5000"))
                .rulePriority(10)
                .build();

        when(tierRepository.findAllByActiveTrueOrderByPriorityDesc())
                .thenReturn(List.of(platinumTier, goldTier, silverTier));
        when(tierRuleRepository.findByTierIdOrderByRulePriorityDesc(3L))
                .thenReturn(List.of(platinumRule));

        TierEvaluationContext context = new TierEvaluationContext(1L, 60, new BigDecimal("6000"), null);
        Optional<MembershipTier> result = engine.evaluate(context);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Platinum");
    }

    @Test
    @DisplayName("Should recommend Gold when only Gold rules satisfied")
    void evaluate_recommendsGold() {
        TierRule platinumRule = TierRule.builder()
                .tier(platinumTier)
                .minimumOrders(50)
                .minimumMonthlySpend(new BigDecimal("5000"))
                .rulePriority(10)
                .build();
        TierRule goldRule = TierRule.builder()
                .tier(goldTier)
                .minimumOrders(20)
                .minimumMonthlySpend(new BigDecimal("2000"))
                .rulePriority(10)
                .build();

        when(tierRepository.findAllByActiveTrueOrderByPriorityDesc())
                .thenReturn(List.of(platinumTier, goldTier, silverTier));
        when(tierRuleRepository.findByTierIdOrderByRulePriorityDesc(3L)).thenReturn(List.of(platinumRule));
        when(tierRuleRepository.findByTierIdOrderByRulePriorityDesc(2L)).thenReturn(List.of(goldRule));

        TierEvaluationContext context = new TierEvaluationContext(1L, 25, new BigDecimal("2500"), null);
        Optional<MembershipTier> result = engine.evaluate(context);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Gold");
    }

    @Test
    @DisplayName("Should qualify via VIP cohort regardless of spend/orders")
    void evaluate_vipCohortQualifiesForPlatinum() {
        TierRule vipRule = TierRule.builder()
                .tier(platinumTier)
                .cohort("VIP")
                .rulePriority(30)
                .build();

        when(tierRepository.findAllByActiveTrueOrderByPriorityDesc())
                .thenReturn(List.of(platinumTier, goldTier, silverTier));
        when(tierRuleRepository.findByTierIdOrderByRulePriorityDesc(3L)).thenReturn(List.of(vipRule));

        TierEvaluationContext context = new TierEvaluationContext(1L, 0, BigDecimal.ZERO, "VIP");
        Optional<MembershipTier> result = engine.evaluate(context);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Platinum");
    }

    @Test
    @DisplayName("Should return empty when no tier qualifies")
    void evaluate_noTierQualifies() {
        TierRule silverRule = TierRule.builder()
                .tier(silverTier)
                .minimumOrders(5)
                .rulePriority(10)
                .build();

        when(tierRepository.findAllByActiveTrueOrderByPriorityDesc())
                .thenReturn(List.of(platinumTier, goldTier, silverTier));
        when(tierRuleRepository.findByTierIdOrderByRulePriorityDesc(3L)).thenReturn(List.of());
        when(tierRuleRepository.findByTierIdOrderByRulePriorityDesc(2L)).thenReturn(List.of());
        when(tierRuleRepository.findByTierIdOrderByRulePriorityDesc(1L)).thenReturn(List.of(silverRule));

        TierEvaluationContext context = new TierEvaluationContext(1L, 2, BigDecimal.ZERO, null);
        Optional<MembershipTier> result = engine.evaluate(context);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("OrderCountStrategy - should return true when null minimum")
    void orderCountStrategy_nullMinimum_returnsTrue() {
        OrderCountStrategy strategy = new OrderCountStrategy();
        TierRule rule = TierRule.builder().minimumOrders(null).build();
        assertThat(strategy.evaluate(new TierEvaluationContext(1L, 0, BigDecimal.ZERO, null), rule)).isTrue();
    }

    @Test
    @DisplayName("CohortStrategy - should be case insensitive")
    void cohortStrategy_caseInsensitive() {
        CohortStrategy strategy = new CohortStrategy();
        TierRule rule = TierRule.builder().cohort("VIP").build();
        assertThat(strategy.evaluate(new TierEvaluationContext(1L, 0, BigDecimal.ZERO, "vip"), rule)).isTrue();
        assertThat(strategy.evaluate(new TierEvaluationContext(1L, 0, BigDecimal.ZERO, "Vip"), rule)).isTrue();
    }
}
