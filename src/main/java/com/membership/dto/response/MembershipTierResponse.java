package com.membership.dto.response;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Response DTO for a membership tier including configurable benefits.
 */
public record MembershipTierResponse(
        Long id,
        String name,
        Integer priority,
        BigDecimal discountPercentage,
        Boolean freeDelivery,
        Boolean prioritySupport,
        Boolean earlyAccess,
        Map<String, Object> configuration,
        Boolean active
) {}
