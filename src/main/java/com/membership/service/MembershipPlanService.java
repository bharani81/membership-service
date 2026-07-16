package com.membership.service;

import com.membership.dto.response.MembershipPlanResponse;

import java.util.List;

/**
 * Application service for membership plan operations.
 */
public interface MembershipPlanService {

    List<MembershipPlanResponse> getAllActivePlans();

    MembershipPlanResponse getPlanById(Long id);
}
