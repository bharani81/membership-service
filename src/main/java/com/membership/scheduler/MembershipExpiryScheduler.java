package com.membership.scheduler;

import com.membership.entity.MembershipHistory;
import com.membership.entity.UserMembership;
import com.membership.entity.enums.MembershipAction;
import com.membership.entity.enums.MembershipStatus;
import com.membership.repository.MembershipHistoryRepository;
import com.membership.repository.UserMembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduled job that marks expired memberships as EXPIRED.
 *
 * <p>Runs daily at 1 AM (configurable via {@code membership.expiry.scheduler.cron}).
 * Finds all memberships past their expiry date with ACTIVE status and transitions
 * them to EXPIRED, recording audit history for each.
 *
 * <p>This is a system-initiated transition, not user-initiated—hence the EXPIRE action
 * in the history rather than CANCEL.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MembershipExpiryScheduler {

    private final UserMembershipRepository membershipRepository;
    private final MembershipHistoryRepository historyRepository;

    @Scheduled(cron = "${membership.expiry.scheduler.cron:0 0 1 * * *}")
    @Transactional
    public void expireOverdueMemberships() {
        LocalDate today = LocalDate.now();
        log.info("Running membership expiry scheduler for date={}", today);

        List<UserMembership> expired = membershipRepository
                .findByExpiryDateBeforeAndStatus(today, MembershipStatus.ACTIVE);

        if (expired.isEmpty()) {
            log.info("No expired memberships found");
            return;
        }

        log.info("Found {} memberships to expire", expired.size());

        for (UserMembership membership : expired) {
            membership.setStatus(MembershipStatus.EXPIRED);
            membershipRepository.save(membership);

            MembershipHistory history = MembershipHistory.builder()
                    .membership(membership)
                    .userId(membership.getUserId())
                    .action(MembershipAction.EXPIRE)
                    .fromPlan(membership.getPlan())
                    .fromTier(membership.getTier())
                    .build();
            historyRepository.save(history);

            log.info("Membership expired: membershipId={} userId={} expiredOn={}",
                    membership.getId(), membership.getUserId(), membership.getExpiryDate());
        }

        log.info("Expiry scheduler completed: {} memberships expired", expired.size());
    }
}
