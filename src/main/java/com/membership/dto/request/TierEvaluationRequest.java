package com.membership.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request body for evaluating which tier a user qualifies for.
 * Runs the Strategy Pattern engine.
 */
public record TierEvaluationRequest(
        @NotNull(message = "orderCount is required")
        @Min(value = 0, message = "orderCount must be >= 0")
        Integer orderCount,

        @NotNull(message = "monthlySpend is required")
        @DecimalMin(value = "0.0", message = "monthlySpend must be >= 0")
        BigDecimal monthlySpend,

        String cohort
) {}
