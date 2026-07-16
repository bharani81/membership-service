package com.membership.repository;

import com.membership.entity.MembershipPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link MembershipPlan} with specification support for composable filtering.
 */
@Repository
public interface MembershipPlanRepository
        extends JpaRepository<MembershipPlan, Long>, JpaSpecificationExecutor<MembershipPlan> {

    List<MembershipPlan> findAllByActiveTrueOrderByDurationMonthsAsc();
}
