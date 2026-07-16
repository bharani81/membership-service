package com.membership.dto.response;

import com.membership.entity.enums.MembershipStatus;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Response DTO for a user's membership subscription.
 */
public record UserMembershipResponse(
        Long id,
        Long userId,
        MembershipPlanResponse plan,
        MembershipTierResponse tier,
        MembershipStatus status,
        LocalDate startDate,
        LocalDate expiryDate,
        Long version,
        Instant createdAt,
        Instant updatedAt
) {}
