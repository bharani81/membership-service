package com.membership.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * A single eligibility rule for a {@link MembershipTier}.
 *
 * <p>Rules are data-driven: to add a new cohort type (e.g., STUDENT, EMPLOYEE, VIP)
 * or threshold, insert a row here. No code changes required.
 *
 * <p>Rule semantics within a tier: ALL rules belonging to a tier must be satisfied
 * (logical AND). Multiple independent qualification paths should use separate tier rules
 * evaluated via the strategy engine.
 */
@Entity
@Table(name = "tier_rules")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TierRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tier_id", nullable = false)
    private MembershipTier tier;

    /** Minimum number of orders required. Null means this rule does not check order count. */
    @Column
    private Integer minimumOrders;

    /** Minimum monthly spend in currency units. Null means unchecked. */
    @Column(precision = 10, scale = 2)
    private BigDecimal minimumMonthlySpend;

    /** Specific user cohort identifier (e.g., VIP, STUDENT, EMPLOYEE). Null means unchecked. */
    @Column(length = 100)
    private String cohort;

    /** Evaluation priority within a tier—higher value = evaluated first. */
    @Column(nullable = false)
    @Builder.Default
    private Integer rulePriority = 0;
}
