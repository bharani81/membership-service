package com.membership.entity;

import com.membership.entity.enums.MembershipStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Core entity representing a user's membership subscription.
 *
 * <p><strong>Concurrency Safety:</strong> The {@code version} field is managed by
 * Hibernate's optimistic locking mechanism ({@link Version}). If two concurrent
 * transactions attempt to modify the same membership (e.g., simultaneous upgrade
 * and cancel), one will succeed and the other will receive an
 * {@link org.springframework.orm.jpa.JpaObjectRetrievalFailureException} which
 * is mapped to HTTP 409 Conflict by the global exception handler.
 *
 * <p>Only one ACTIVE membership per user is enforced at the database level via
 * a partial unique index.
 */
@Entity
@Table(name = "user_memberships")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private MembershipPlan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tier_id", nullable = false)
    private MembershipTier tier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MembershipStatus status;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate expiryDate;

    /**
     * Optimistic locking version. Hibernate increments this on every UPDATE.
     * Concurrent modifications to the same row will cause one to fail with
     * OptimisticLockException → caught → HTTP 409 Conflict.
     */
    @Version
    @Column(nullable = false)
    private Long version;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;
}
