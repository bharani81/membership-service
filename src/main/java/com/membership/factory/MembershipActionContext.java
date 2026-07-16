package com.membership.factory;

import com.membership.entity.MembershipPlan;
import com.membership.entity.MembershipTier;
import com.membership.entity.UserMembership;

/**
 * Immutable context object passed to {@link MembershipActionHandler} implementations.
 *
 * <p>Using a Java record ensures this value object is thread-safe and its fields
 * cannot be accidentally mutated during handler processing.
 *
 * @param existingMembership the current membership (null for new subscriptions)
 * @param targetPlan         the plan to subscribe/upgrade/downgrade to
 * @param targetTier         the tier to assign
 * @param userId             the user performing the action
 */
public record MembershipActionContext(
        UserMembership existingMembership,
        MembershipPlan targetPlan,
        MembershipTier targetTier,
        Long userId
) {}
