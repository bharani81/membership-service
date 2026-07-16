package com.membership.service.impl;

import com.membership.dto.response.MembershipPlanResponse;
import com.membership.entity.MembershipPlan;
import com.membership.exception.PlanNotFoundException;
import com.membership.mapper.MembershipPlanMapper;
import com.membership.repository.MembershipPlanRepository;
import com.membership.service.MembershipPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link MembershipPlanService}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MembershipPlanServiceImpl implements MembershipPlanService {

    private final MembershipPlanRepository planRepository;
    private final MembershipPlanMapper planMapper;

    @Override
    public List<MembershipPlanResponse> getAllActivePlans() {
        log.debug("Fetching all active membership plans");
        List<MembershipPlan> plans = planRepository.findAllByActiveTrueOrderByDurationMonthsAsc();
        log.info("Retrieved {} active plans", plans.size());
        return planMapper.toResponseList(plans);
    }

    @Override
    public MembershipPlanResponse getPlanById(Long id) {
        log.debug("Fetching plan by id={}", id);
        MembershipPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new PlanNotFoundException(id));
        return planMapper.toResponse(plan);
    }
}
