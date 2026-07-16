package com.membership.repository.spec;

import com.membership.entity.MembershipPlan;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

/**
 * Composable JPA Specifications for {@link MembershipPlan} queries.
 *
 * <p>Follows the Specification Pattern to enable flexible, composable filtering
 * without bloating the repository with dozens of query methods.
 *
 * <p>Usage:
 * <pre>{@code
 *   Specification<MembershipPlan> spec = MembershipPlanSpecification.isActive()
 *       .and(MembershipPlanSpecification.hasPriceLessThan(new BigDecimal("500")));
 *   planRepository.findAll(spec);
 * }</pre>
 */
public final class MembershipPlanSpecification {

    private MembershipPlanSpecification() {
        // Utility class — do not instantiate
    }

    public static Specification<MembershipPlan> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    public static Specification<MembershipPlan> hasDurationMonths(int months) {
        return (root, query, cb) -> cb.equal(root.get("durationMonths"), months);
    }

    public static Specification<MembershipPlan> hasPriceLessThanOrEqual(BigDecimal maxPrice) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<MembershipPlan> hasNameContaining(String name) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }
}
