package com.membership.service;

import com.membership.dto.request.SubscribeRequest;
import com.membership.dto.request.UpgradeRequest;
import com.membership.dto.response.UserMembershipResponse;
import com.membership.entity.MembershipPlan;
import com.membership.entity.MembershipTier;
import com.membership.entity.UserMembership;
import com.membership.entity.enums.MembershipAction;
import com.membership.entity.enums.MembershipStatus;
import com.membership.exception.MembershipNotFoundException;
import com.membership.exception.SubscriptionAlreadyExistsException;
import com.membership.factory.MembershipActionFactory;
import com.membership.factory.MembershipActionHandler;
import com.membership.mapper.UserMembershipMapper;
import com.membership.repository.MembershipHistoryRepository;
import com.membership.repository.MembershipPlanRepository;
import com.membership.repository.MembershipTierRepository;
import com.membership.repository.UserMembershipRepository;
import com.membership.service.impl.UserMembershipServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserMembershipService Unit Tests")
class UserMembershipServiceTest {

    @Mock private UserMembershipRepository membershipRepository;
    @Mock private MembershipPlanRepository planRepository;
    @Mock private MembershipTierRepository tierRepository;
    @Mock private MembershipHistoryRepository historyRepository;
    @Mock private MembershipActionFactory actionFactory;
    @Mock private MembershipActionHandler actionHandler;
    @Mock private UserMembershipMapper membershipMapper;

    @InjectMocks
    private UserMembershipServiceImpl membershipService;

    private MembershipPlan testPlan;
    private MembershipTier silverTier;
    private MembershipTier goldTier;
    private UserMembership testMembership;

    @BeforeEach
    void setUp() {
        testPlan = MembershipPlan.builder()
                .id(1L)
                .name("Monthly")
                .durationMonths(1)
                .price(new BigDecimal("99.00"))
                .active(true)
                .build();

        silverTier = MembershipTier.builder()
                .id(1L)
                .name("Silver")
                .priority(1)
                .discountPercentage(new BigDecimal("5.00"))
                .active(true)
                .build();

        goldTier = MembershipTier.builder()
                .id(2L)
                .name("Gold")
                .priority(2)
                .discountPercentage(new BigDecimal("10.00"))
                .active(true)
                .build();

        testMembership = UserMembership.builder()
                .id(1L)
                .userId(100L)
                .plan(testPlan)
                .tier(silverTier)
                .status(MembershipStatus.ACTIVE)
                .startDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusMonths(1))
                .version(0L)
                .build();
    }

    @Test
    @DisplayName("Subscribe - should create membership successfully")
    void subscribe_success() {
        SubscribeRequest request = new SubscribeRequest(100L, 1L, 1L);

        when(planRepository.findById(1L)).thenReturn(Optional.of(testPlan));
        when(tierRepository.findById(1L)).thenReturn(Optional.of(silverTier));
        when(actionFactory.getHandler(MembershipAction.SUBSCRIBE)).thenReturn(actionHandler);
        when(actionHandler.handle(any())).thenReturn(testMembership);
        when(historyRepository.save(any())).thenReturn(null);
        when(membershipMapper.toResponse(testMembership)).thenReturn(buildMockResponse());

        UserMembershipResponse response = membershipService.subscribe(request);

        assertThat(response).isNotNull();
        verify(actionFactory).getHandler(MembershipAction.SUBSCRIBE);
        verify(historyRepository).save(any());
    }

    @Test
    @DisplayName("Subscribe - should throw when plan not found")
    void subscribe_planNotFound() {
        SubscribeRequest request = new SubscribeRequest(100L, 999L, 1L);
        when(planRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> membershipService.subscribe(request))
                .isInstanceOf(com.membership.exception.PlanNotFoundException.class);
    }

    @Test
    @DisplayName("Cancel - should cancel active membership")
    void cancel_success() {
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(testMembership));
        when(actionFactory.getHandler(MembershipAction.CANCEL)).thenReturn(actionHandler);
        when(actionHandler.handle(any())).thenReturn(testMembership);
        when(historyRepository.save(any())).thenReturn(null);
        when(membershipMapper.toResponse(testMembership)).thenReturn(buildMockResponse());

        UserMembershipResponse response = membershipService.cancel(1L);

        assertThat(response).isNotNull();
        verify(actionFactory).getHandler(MembershipAction.CANCEL);
    }

    @Test
    @DisplayName("Cancel - should throw when membership not found")
    void cancel_notFound() {
        when(membershipRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> membershipService.cancel(999L))
                .isInstanceOf(MembershipNotFoundException.class);
    }

    @Test
    @DisplayName("GetCurrentMembership - should throw when no active membership")
    void getCurrentMembership_noActiveMembership() {
        when(membershipRepository.findByUserIdAndStatus(100L, MembershipStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> membershipService.getCurrentMembership(100L))
                .isInstanceOf(MembershipNotFoundException.class)
                .hasMessageContaining("No active membership");
    }

    @Test
    @DisplayName("GetMembershipStatus - should return expiry details")
    void getMembershipStatus_expiringIn5Days() {
        UserMembership expiringMembership = UserMembership.builder()
                .id(1L)
                .userId(100L)
                .plan(testPlan)
                .tier(silverTier)
                .status(MembershipStatus.ACTIVE)
                .startDate(LocalDate.now().minusDays(25))
                .expiryDate(LocalDate.now().plusDays(5))
                .version(0L)
                .build();

        when(membershipRepository.findByUserIdAndStatus(100L, MembershipStatus.ACTIVE))
                .thenReturn(Optional.of(expiringMembership));

        var status = membershipService.getMembershipStatus(100L);

        assertThat(status.expiringSoon()).isTrue();
        assertThat(status.expired()).isFalse();
        assertThat(status.daysUntilExpiry()).isEqualTo(5L);
    }

    @Test
    @DisplayName("Upgrade - should fetch correct plan and tier")
    void upgrade_success() {
        UpgradeRequest request = new UpgradeRequest(1L, 2L);

        when(membershipRepository.findById(1L)).thenReturn(Optional.of(testMembership));
        when(planRepository.findById(1L)).thenReturn(Optional.of(testPlan));
        when(tierRepository.findById(2L)).thenReturn(Optional.of(goldTier));
        when(actionFactory.getHandler(MembershipAction.UPGRADE)).thenReturn(actionHandler);
        when(actionHandler.handle(any())).thenReturn(testMembership);
        when(historyRepository.save(any())).thenReturn(null);
        when(membershipMapper.toResponse(testMembership)).thenReturn(buildMockResponse());

        UserMembershipResponse response = membershipService.upgrade(1L, request);

        assertThat(response).isNotNull();
        verify(actionFactory).getHandler(MembershipAction.UPGRADE);
    }

    private UserMembershipResponse buildMockResponse() {
        return new UserMembershipResponse(
                1L, 100L, null, null,
                MembershipStatus.ACTIVE,
                LocalDate.now(), LocalDate.now().plusMonths(1),
                0L, null, null
        );
    }
}
