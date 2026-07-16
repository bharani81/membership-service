package com.membership.factory.impl;

import com.membership.entity.UserMembership;
import com.membership.entity.enums.MembershipAction;
import com.membership.entity.enums.MembershipStatus;
import com.membership.exception.SubscriptionAlreadyExistsException;
import com.membership.factory.MembershipActionContext;
import com.membership.factory.MembershipActionHandler;
import com.membership.repository.UserMembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Handles new membership subscriptions.
 *
 * <p>Validates that the user does not already have an active membership,
 * then creates a new {@link UserMembership} with ACTIVE status.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscribeActionHandler implements MembershipActionHandler {

    private final UserMembershipRepository membershipRepository;

    @Override
    public UserMembership handle(MembershipActionContext context) {
        Long userId = context.userId();

        if (membershipRepository.existsByUserIdAndStatus(userId, MembershipStatus.ACTIVE)) {
            throw new SubscriptionAlreadyExistsException(
                    "User " + userId + " already has an active membership. Cancel it before subscribing to a new plan.");
        }

        LocalDate startDate = LocalDate.now();
        LocalDate expiryDate = startDate.plusMonths(context.targetPlan().getDurationMonths());

        UserMembership membership = UserMembership.builder()
                .userId(userId)
                .plan(context.targetPlan())
                .tier(context.targetTier())
                .status(MembershipStatus.ACTIVE)
                .startDate(startDate)
                .expiryDate(expiryDate)
                .build();

        UserMembership saved = membershipRepository.save(membership);
        log.info("Subscription created: membershipId={} userId={} plan={} tier={} expiryDate={}",
                saved.getId(), userId, context.targetPlan().getName(),
                context.targetTier().getName(), expiryDate);
        return saved;
    }

    @Override
    public MembershipAction supportedAction() {
        return MembershipAction.SUBSCRIBE;
    }
}
