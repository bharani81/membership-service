package com.membership.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request body for creating a new membership subscription.
 */
public record SubscribeRequest(
        @NotNull(message = "userId is required")
        @Positive(message = "userId must be a positive number")
        Long userId,

        @NotNull(message = "planId is required")
        @Positive(message = "planId must be a positive number")
        Long planId,

        @NotNull(message = "tierId is required")
        @Positive(message = "tierId must be a positive number")
        Long tierId
) {}
