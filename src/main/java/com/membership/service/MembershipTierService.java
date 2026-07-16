package com.membership.service;

import com.membership.dto.response.MembershipTierResponse;

import java.util.List;

/**
 * Application service for membership tier operations.
 */
public interface MembershipTierService {

    List<MembershipTierResponse> getAllActiveTiers();

    MembershipTierResponse getTierById(Long id);
}
