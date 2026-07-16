package com.membership.service;

import com.membership.dto.request.DowngradeRequest;
import com.membership.dto.request.SubscribeRequest;
import com.membership.dto.request.UpgradeRequest;
import com.membership.dto.response.MembershipStatusResponse;
import com.membership.dto.response.UserMembershipResponse;

/**
 * Application service for user membership lifecycle operations.
 */
public interface UserMembershipService {

    UserMembershipResponse subscribe(SubscribeRequest request);

    UserMembershipResponse upgrade(Long membershipId, UpgradeRequest request);

    UserMembershipResponse downgrade(Long membershipId, DowngradeRequest request);

    UserMembershipResponse cancel(Long membershipId);

    UserMembershipResponse getCurrentMembership(Long userId);

    MembershipStatusResponse getMembershipStatus(Long userId);
}
