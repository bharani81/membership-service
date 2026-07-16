package com.membership.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response DTO for a membership plan.
 */
public record MembershipPlanResponse(
        Long id,
        String name,
        Integer durationMonths,
        BigDecimal price,
        Boolean active,
        Instant createdAt
) {}
