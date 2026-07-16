package com.membership.dto.response;

/**
 * Response DTO for tier evaluation results.
 */
public record TierEvaluationResponse(
        Long userId,
        MembershipTierResponse recommendedTier,
        boolean tierFound,
        String message
) {}
