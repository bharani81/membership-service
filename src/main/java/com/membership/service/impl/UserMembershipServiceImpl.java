package com.membership.service.impl;

import com.membership.dto.request.DowngradeRequest;
import com.membership.dto.request.SubscribeRequest;
import com.membership.dto.request.UpgradeRequest;
import com.membership.dto.response.MembershipStatusResponse;
import com.membership.dto.response.UserMembershipResponse;
import com.membership.entity.MembershipHistory;
import com.membership.entity.MembershipPlan;
import com.membership.entity.MembershipTier;
import com.membership.entity.UserMembership;
import com.membership.entity.enums.MembershipAction;
import com.membership.entity.enums.MembershipStatus;
import com.membership.exception.MembershipNotFoundException;
import com.membership.exception.PlanNotFoundException;
import com.membership.exception.TierNotFoundException;
import com.membership.factory.MembershipActionContext;
import com.membership.factory.MembershipActionFactory;
import com.membership.mapper.UserMembershipMapper;
import com.membership.repository.MembershipHistoryRepository;
import com.membership.repository.MembershipPlanRepository;
import com.membership.repository.MembershipTierRepository;
import com.membership.repository.UserMembershipRepository;
import com.membership.service.UserMembershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Core application service for user membership lifecycle management.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Orchestrate the Factory pattern to dispatch lifecycle actions</li>
 *   <li>Record audit history after every state change</li>
 *   <li>Maintain {@code @Transactional} boundaries (never the controller)</li>
 * </ul>
 *
 * <p>Concurrent modifications (e.g., simultaneous upgrade + cancel) are handled
 * at the entity level via {@code @Version} optimistic locking. The
 * {@link com.membership.exception.GlobalExceptionHandler} catches the resulting
 * {@link org.springframework.orm.ObjectOptimisticLockingFailureException} and
 * returns HTTP 409 Conflict.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserMembershipServiceImpl implements UserMembershipService {

    private final UserMembershipRepository membershipRepository;
    private final MembershipPlanRepository planRepository;
    private final MembershipTierRepository tierRepository;
    private final MembershipHistoryRepository historyRepository;
    private final MembershipActionFactory actionFactory;
    private final UserMembershipMapper membershipMapper;

    @Override
    public UserMembershipResponse subscribe(SubscribeRequest request) {
        MembershipPlan plan = resolvePlan(request.planId());
        MembershipTier tier = resolveTier(request.tierId());

        MembershipActionContext context = new MembershipActionContext(null, plan, tier, request.userId());
        UserMembership membership = actionFactory.getHandler(MembershipAction.SUBSCRIBE).handle(context);

        recordHistory(membership, MembershipAction.SUBSCRIBE, null, plan, null, tier);
        log.info("Subscription created: membershipId={} userId={}", membership.getId(), request.userId());
        return membershipMapper.toResponse(membership);
    }

    @Override
    public UserMembershipResponse upgrade(Long membershipId, UpgradeRequest request) {
        UserMembership membership = resolveMembership(membershipId);
        MembershipPlan targetPlan = resolvePlan(request.targetPlanId());
        MembershipTier targetTier = resolveTier(request.targetTierId());

        MembershipPlan fromPlan = membership.getPlan();
        MembershipTier fromTier = membership.getTier();

        MembershipActionContext context = new MembershipActionContext(membership, targetPlan, targetTier, membership.getUserId());
        UserMembership upgraded = actionFactory.getHandler(MembershipAction.UPGRADE).handle(context);

        recordHistory(upgraded, MembershipAction.UPGRADE, fromPlan, targetPlan, fromTier, targetTier);
        log.info("Membership upgraded: membershipId={} userId={} fromTier={} toTier={}",
                membershipId, upgraded.getUserId(), fromTier.getName(), targetTier.getName());
        return membershipMapper.toResponse(upgraded);
    }

    @Override
    public UserMembershipResponse downgrade(Long membershipId, DowngradeRequest request) {
        UserMembership membership = resolveMembership(membershipId);
        MembershipPlan targetPlan = resolvePlan(request.targetPlanId());
        MembershipTier targetTier = resolveTier(request.targetTierId());

        MembershipPlan fromPlan = membership.getPlan();
        MembershipTier fromTier = membership.getTier();

        MembershipActionContext context = new MembershipActionContext(membership, targetPlan, targetTier, membership.getUserId());
        UserMembership downgraded = actionFactory.getHandler(MembershipAction.DOWNGRADE).handle(context);

        recordHistory(downgraded, MembershipAction.DOWNGRADE, fromPlan, targetPlan, fromTier, targetTier);
        log.info("Membership downgraded: membershipId={} userId={} fromTier={} toTier={}",
                membershipId, downgraded.getUserId(), fromTier.getName(), targetTier.getName());
        return membershipMapper.toResponse(downgraded);
    }

    @Override
    public UserMembershipResponse cancel(Long membershipId) {
        UserMembership membership = resolveMembership(membershipId);

        MembershipActionContext context = new MembershipActionContext(membership, membership.getPlan(), membership.getTier(), membership.getUserId());
        UserMembership cancelled = actionFactory.getHandler(MembershipAction.CANCEL).handle(context);

        recordHistory(cancelled, MembershipAction.CANCEL, membership.getPlan(), null, membership.getTier(), null);
        log.info("Membership cancelled: membershipId={} userId={}", membershipId, cancelled.getUserId());
        return membershipMapper.toResponse(cancelled);
    }

    @Override
    @Transactional(readOnly = true)
    public UserMembershipResponse getCurrentMembership(Long userId) {
        UserMembership membership = membershipRepository
                .findByUserIdAndStatus(userId, MembershipStatus.ACTIVE)
                .orElseThrow(() -> new MembershipNotFoundException(
                        "No active membership found for userId: " + userId));
        return membershipMapper.toResponse(membership);
    }

    @Override
    @Transactional(readOnly = true)
    public MembershipStatusResponse getMembershipStatus(Long userId) {
        UserMembership membership = membershipRepository
                .findByUserIdAndStatus(userId, MembershipStatus.ACTIVE)
                .orElseThrow(() -> new MembershipNotFoundException(
                        "No active membership found for userId: " + userId));

        LocalDate today = LocalDate.now();
        long daysUntilExpiry = ChronoUnit.DAYS.between(today, membership.getExpiryDate());
        boolean expired = daysUntilExpiry < 0;
        boolean expiringSoon = !expired && daysUntilExpiry <= 7;

        return new MembershipStatusResponse(
                membership.getId(),
                membership.getUserId(),
                membership.getStatus(),
                membership.getExpiryDate(),
                daysUntilExpiry,
                expiringSoon,
                expired
        );
    }

    // ========================
    // Private helpers
    // ========================

    private UserMembership resolveMembership(Long id) {
        return membershipRepository.findById(id)
                .orElseThrow(() -> new MembershipNotFoundException(id));
    }

    private MembershipPlan resolvePlan(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new PlanNotFoundException(id));
    }

    private MembershipTier resolveTier(Long id) {
        return tierRepository.findById(id)
                .orElseThrow(() -> new TierNotFoundException(id));
    }

    private void recordHistory(UserMembership membership, MembershipAction action,
                                MembershipPlan fromPlan, MembershipPlan toPlan,
                                MembershipTier fromTier, MembershipTier toTier) {
        MembershipHistory history = MembershipHistory.builder()
                .membership(membership)
                .userId(membership.getUserId())
                .action(action)
                .fromPlan(fromPlan)
                .toPlan(toPlan)
                .fromTier(fromTier)
                .toTier(toTier)
                .build();
        historyRepository.save(history);
    }
}
