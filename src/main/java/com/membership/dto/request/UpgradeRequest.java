package com.membership.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request body for upgrading a membership to a higher plan/tier.
 */
public record UpgradeRequest(
        @NotNull(message = "targetPlanId is required")
        @Positive(message = "targetPlanId must be a positive number")
        Long targetPlanId,

        @NotNull(message = "targetTierId is required")
        @Positive(message = "targetTierId must be a positive number")
        Long targetTierId
) {}
