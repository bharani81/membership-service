package com.membership.entity;

import com.membership.entity.converter.JsonAttributeConverter;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a membership tier (Silver, Gold, Platinum).
 *
 * <p>Benefits are intentionally stored in {@code configuration} as a JSONB map
 * so that new benefit attributes (e.g., loungeAccess, conciergeSupport) can be
 * added without schema migrations or code changes to this class.
 */
@Entity
@Table(name = "membership_tiers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MembershipTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    @ToString.Include
    private String name;

    /** Higher priority = higher tier. Used for upgrade/downgrade validation. */
    @Column(nullable = false)
    private Integer priority;

    @Column(nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Boolean freeDelivery = Boolean.FALSE;

    @Column(nullable = false)
    @Builder.Default
    private Boolean prioritySupport = Boolean.FALSE;

    @Column(nullable = false)
    @Builder.Default
    private Boolean earlyAccess = Boolean.FALSE;

    /**
     * Extensible benefit configuration stored as JSONB.
     * Allows new attributes without schema or code changes.
     */
    @Convert(converter = JsonAttributeConverter.class)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> configuration = new HashMap<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = Boolean.TRUE;
}
