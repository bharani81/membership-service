package com.membership.factory.impl;

import com.membership.entity.UserMembership;
import com.membership.entity.enums.MembershipAction;
import com.membership.entity.enums.MembershipStatus;
import com.membership.exception.MembershipException;
import com.membership.factory.MembershipActionContext;
import com.membership.factory.MembershipActionHandler;
import com.membership.repository.UserMembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Handles membership cancellations.
 *
 * <p>Only ACTIVE or PENDING memberships can be cancelled.
 * EXPIRED and already-CANCELLED memberships are rejected.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CancelActionHandler implements MembershipActionHandler {

    private final UserMembershipRepository membershipRepository;

    @Override
    public UserMembership handle(MembershipActionContext context) {
        UserMembership membership = context.existingMembership();

        if (membership.getStatus() == MembershipStatus.CANCELLED) {
            throw new MembershipException("Membership is already cancelled.", HttpStatus.CONFLICT);
        }
        if (membership.getStatus() == MembershipStatus.EXPIRED) {
            throw new MembershipException("Cannot cancel an already expired membership.", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        membership.setStatus(MembershipStatus.CANCELLED);

        UserMembership saved = membershipRepository.save(membership);
        log.info("Membership cancelled: membershipId={} userId={}",
                saved.getId(), saved.getUserId());
        return saved;
    }

    @Override
    public MembershipAction supportedAction() {
        return MembershipAction.CANCEL;
    }
}
