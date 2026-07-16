package com.membership.repository;

import com.membership.entity.TierRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link TierRule}.
 * Rules are fetched ordered by priority descending so the strategy engine
 * evaluates the most important rules first.
 */
@Repository
public interface TierRuleRepository extends JpaRepository<TierRule, Long> {

    List<TierRule> findByTierIdOrderByRulePriorityDesc(Long tierId);
}
