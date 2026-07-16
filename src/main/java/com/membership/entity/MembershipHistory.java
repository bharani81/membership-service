package com.membership.entity;

import com.membership.entity.enums.MembershipAction;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Map;

import com.membership.entity.converter.JsonAttributeConverter;

/**
 * Immutable audit log entry for every membership state transition.
 *
 * <p>This table is append-only — no UPDATE or DELETE operations are ever performed.
 * All columns are marked {@code updatable = false} to enforce this at the ORM level.
 */
@Entity
@Table(name = "membership_history")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MembershipHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_id", nullable = false, updatable = false)
    private UserMembership membership;

    @Column(nullable = false, updatable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private MembershipAction action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_plan_id", updatable = false)
    private MembershipPlan fromPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_plan_id", updatable = false)
    private MembershipPlan toPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_tier_id", updatable = false)
    private MembershipTier fromTier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_tier_id", updatable = false)
    private MembershipTier toTier;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant performedAt = Instant.now();

    @Convert(converter = JsonAttributeConverter.class)
    @Column(columnDefinition = "jsonb", updatable = false)
    private Map<String, Object> metadata;
}
