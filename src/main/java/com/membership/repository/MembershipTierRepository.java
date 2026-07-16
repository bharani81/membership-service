package com.membership.repository;

import com.membership.entity.MembershipTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link MembershipTier}.
 */
@Repository
public interface MembershipTierRepository extends JpaRepository<MembershipTier, Long> {

    List<MembershipTier> findAllByActiveTrueOrderByPriorityDesc();
}
