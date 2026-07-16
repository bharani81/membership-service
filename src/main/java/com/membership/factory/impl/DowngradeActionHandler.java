package com.membership.factory.impl;

import com.membership.entity.UserMembership;
import com.membership.entity.enums.MembershipAction;
import com.membership.entity.enums.MembershipStatus;
import com.membership.exception.InvalidDowngradeException;
import com.membership.exception.MembershipExpiredException;
import com.membership.factory.MembershipActionContext;
import com.membership.factory.MembershipActionHandler;
import com.membership.repository.UserMembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handles membership downgrades to a lower-priority tier.
 *
 * <p>Validation rules:
 * <ul>
 *   <li>Membership must be ACTIVE.</li>
 *   <li>Target tier priority must be strictly less than the current tier priority.</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DowngradeActionHandler implements MembershipActionHandler {

    private final UserMembershipRepository membershipRepository;

    @Override
    public UserMembership handle(MembershipActionContext context) {
        UserMembership membership = context.existingMembership();

        if (membership.getStatus() != MembershipStatus.ACTIVE) {
            throw new MembershipExpiredException(
                    "Cannot downgrade a membership with status: " + membership.getStatus());
        }

        int currentPriority = membership.getTier().getPriority();
        int targetPriority = context.targetTier().getPriority();

        if (targetPriority >= currentPriority) {
            throw new InvalidDowngradeException(String.format(
                    "Cannot downgrade from tier '%s' (priority %d) to tier '%s' (priority %d). Target tier must have lower priority.",
                    membership.getTier().getName(), currentPriority,
                    context.targetTier().getName(), targetPriority));
        }

        membership.setPlan(context.targetPlan());
        membership.setTier(context.targetTier());

        UserMembership saved = membershipRepository.save(membership);
        log.info("Membership downgraded: membershipId={} userId={} fromTier={} toTier={}",
                saved.getId(), saved.getUserId(),
                membership.getTier().getName(), context.targetTier().getName());
        return saved;
    }

    @Override
    public MembershipAction supportedAction() {
        return MembershipAction.DOWNGRADE;
    }
}
