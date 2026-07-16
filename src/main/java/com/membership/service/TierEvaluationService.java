package com.membership.service;

import com.membership.dto.request.TierEvaluationRequest;
import com.membership.dto.response.TierEvaluationResponse;

/**
 * Application service for tier evaluation using the Strategy Pattern engine.
 */
public interface TierEvaluationService {

    TierEvaluationResponse evaluateTier(Long userId, TierEvaluationRequest request);
}
