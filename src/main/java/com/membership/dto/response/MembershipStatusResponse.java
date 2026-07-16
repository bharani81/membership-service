package com.membership.dto.response;

import com.membership.entity.enums.MembershipStatus;

import java.time.LocalDate;

/**
 * Response DTO for membership status check endpoint.
 * Includes derived fields like days until expiry for convenience.
 */
public record MembershipStatusResponse(
        Long membershipId,
        Long userId,
        MembershipStatus status,
        LocalDate expiryDate,
        long daysUntilExpiry,
        boolean expiringSoon,     // true if expiring within 7 days
        boolean expired
) {}
