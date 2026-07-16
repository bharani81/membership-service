package com.membership.service.impl;

import com.membership.dto.request.TierEvaluationRequest;
import com.membership.dto.response.TierEvaluationResponse;
import com.membership.entity.MembershipTier;
import com.membership.mapper.MembershipTierMapper;
import com.membership.service.TierEvaluationService;
import com.membership.strategy.TierEvaluationContext;
import com.membership.strategy.TierEvaluationEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Application service that delegates to the {@link TierEvaluationEngine} for tier computation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TierEvaluationServiceImpl implements TierEvaluationService {

    private final TierEvaluationEngine evaluationEngine;
    private final MembershipTierMapper tierMapper;

    @Override
    public TierEvaluationResponse evaluateTier(Long userId, TierEvaluationRequest request) {
        TierEvaluationContext context = new TierEvaluationContext(
                userId,
                request.orderCount(),
                request.monthlySpend(),
                request.cohort()
        );

        Optional<MembershipTier> recommendedTier = evaluationEngine.evaluate(context);

        return recommendedTier
                .map(tier -> new TierEvaluationResponse(
                        userId,
                        tierMapper.toResponse(tier),
                        true,
                        "Based on your activity, you qualify for the " + tier.getName() + " tier."
                ))
                .orElse(new TierEvaluationResponse(
                        userId,
                        null,
                        false,
                        "No tier qualification found based on current activity metrics."
                ));
    }
}
