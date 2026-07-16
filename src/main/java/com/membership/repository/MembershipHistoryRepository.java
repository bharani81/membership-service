package com.membership.repository;

import com.membership.entity.MembershipHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for the append-only {@link MembershipHistory} audit log.
 * No update or delete operations are exposed.
 */
@Repository
public interface MembershipHistoryRepository extends JpaRepository<MembershipHistory, Long> {

    List<MembershipHistory> findByUserIdOrderByPerformedAtDesc(Long userId);

    List<MembershipHistory> findByMembershipIdOrderByPerformedAtDesc(Long membershipId);
}
