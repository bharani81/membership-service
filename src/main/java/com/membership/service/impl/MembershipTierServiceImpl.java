package com.membership.service.impl;

import com.membership.dto.response.MembershipTierResponse;
import com.membership.entity.MembershipTier;
import com.membership.exception.TierNotFoundException;
import com.membership.mapper.MembershipTierMapper;
import com.membership.repository.MembershipTierRepository;
import com.membership.service.MembershipTierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link MembershipTierService}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MembershipTierServiceImpl implements MembershipTierService {

    private final MembershipTierRepository tierRepository;
    private final MembershipTierMapper tierMapper;

    @Override
    public List<MembershipTierResponse> getAllActiveTiers() {
        log.debug("Fetching all active membership tiers");
        List<MembershipTier> tiers = tierRepository.findAllByActiveTrueOrderByPriorityDesc();
        log.info("Retrieved {} active tiers", tiers.size());
        return tierMapper.toResponseList(tiers);
    }

    @Override
    public MembershipTierResponse getTierById(Long id) {
        log.debug("Fetching tier by id={}", id);
        MembershipTier tier = tierRepository.findById(id)
                .orElseThrow(() -> new TierNotFoundException(id));
        return tierMapper.toResponse(tier);
    }
}
