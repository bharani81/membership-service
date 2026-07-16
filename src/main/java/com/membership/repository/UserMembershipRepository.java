package com.membership.repository;

import com.membership.entity.UserMembership;
import com.membership.entity.enums.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link UserMembership}.
 *
 * <p>Queries are optimized to fetch associated Plan and Tier in a single join
 * where possible to avoid N+1 issues.
 */
@Repository
public interface UserMembershipRepository extends JpaRepository<UserMembership, Long> {

    @Query("""
            SELECT um FROM UserMembership um
            JOIN FETCH um.plan
            JOIN FETCH um.tier
            WHERE um.userId = :userId AND um.status = :status
            """)
    Optional<UserMembership> findByUserIdAndStatus(Long userId, MembershipStatus status);

    /**
     * Used by the expiry scheduler to find memberships that have passed their expiry date.
     */
    @Query("""
            SELECT um FROM UserMembership um
            WHERE um.expiryDate < :date AND um.status = :status
            """)
    List<UserMembership> findByExpiryDateBeforeAndStatus(LocalDate date, MembershipStatus status);

    boolean existsByUserIdAndStatus(Long userId, MembershipStatus status);
}
