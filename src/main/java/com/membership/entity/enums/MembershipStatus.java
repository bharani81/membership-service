package com.membership.entity.enums;

/**
 * Lifecycle states of a user membership.
 */
public enum MembershipStatus {
    /** Membership is current and valid. */
    ACTIVE,
    /** Membership has passed its expiry date. */
    EXPIRED,
    /** User explicitly cancelled the membership. */
    CANCELLED,
    /** Membership created but not yet activated. */
    PENDING
}
