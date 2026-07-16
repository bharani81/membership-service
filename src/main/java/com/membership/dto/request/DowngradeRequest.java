package com.membership.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request body for downgrading a membership to a lower plan/tier.
 */
public record DowngradeRequest(
        @NotNull(message = "targetPlanId is required")
        @Positive(message = "targetPlanId must be a positive number")
        Long targetPlanId,

        @NotNull(message = "targetTierId is required")
        @Positive(message = "targetTierId must be a positive number")
        Long targetTierId
) {}
