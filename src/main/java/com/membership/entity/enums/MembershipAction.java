package com.membership.entity.enums;

/**
 * All possible actions that can be recorded in the membership audit history.
 */
public enum MembershipAction {
    /** New subscription created. */
    SUBSCRIBE,
    /** Moved to a higher plan or tier. */
    UPGRADE,
    /** Moved to a lower plan or tier. */
    DOWNGRADE,
    /** User-initiated cancellation. */
    CANCEL,
    /** Subscription renewed for another period. */
    RENEW,
    /** System-detected expiry (handled by scheduler). */
    EXPIRE
}
