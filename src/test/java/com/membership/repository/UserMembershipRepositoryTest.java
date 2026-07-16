package com.membership.repository;

import com.membership.entity.MembershipPlan;
import com.membership.entity.MembershipTier;
import com.membership.entity.UserMembership;
import com.membership.entity.enums.MembershipStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository integration tests using a real PostgreSQL database via Testcontainers.
 * No H2. No compromises on test fidelity.
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("UserMembership Repository Integration Tests")
class UserMembershipRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("membership_test")
            .withUsername("test_user")
            .withPassword("test_pass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired private UserMembershipRepository membershipRepository;
    @Autowired private MembershipPlanRepository planRepository;
    @Autowired private MembershipTierRepository tierRepository;

    @Test
    @DisplayName("Should find active membership by userId")
    void findByUserIdAndStatus_active() {
        MembershipPlan plan = getOrFetchPlan();
        MembershipTier tier = getOrFetchTier();

        UserMembership membership = UserMembership.builder()
                .userId(500L)
                .plan(plan)
                .tier(tier)
                .status(MembershipStatus.ACTIVE)
                .startDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusMonths(1))
                .build();
        membershipRepository.save(membership);

        Optional<UserMembership> found = membershipRepository.findByUserIdAndStatus(500L, MembershipStatus.ACTIVE);
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(500L);
        assertThat(found.get().getStatus()).isEqualTo(MembershipStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should find memberships past expiry date")
    void findByExpiryDateBeforeAndStatus() {
        MembershipPlan plan = getOrFetchPlan();
        MembershipTier tier = getOrFetchTier();

        UserMembership expiredMembership = UserMembership.builder()
                .userId(600L)
                .plan(plan)
                .tier(tier)
                .status(MembershipStatus.ACTIVE)
                .startDate(LocalDate.now().minusMonths(2))
                .expiryDate(LocalDate.now().minusDays(5)) // Past expiry
                .build();
        membershipRepository.save(expiredMembership);

        List<UserMembership> found = membershipRepository
                .findByExpiryDateBeforeAndStatus(LocalDate.now(), MembershipStatus.ACTIVE);

        assertThat(found).isNotEmpty();
        assertThat(found).anyMatch(m -> m.getUserId().equals(600L));
    }

    @Test
    @DisplayName("Should verify existence of active membership")
    void existsByUserIdAndStatus() {
        MembershipPlan plan = getOrFetchPlan();
        MembershipTier tier = getOrFetchTier();

        UserMembership membership = UserMembership.builder()
                .userId(700L)
                .plan(plan)
                .tier(tier)
                .status(MembershipStatus.ACTIVE)
                .startDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusMonths(1))
                .build();
        membershipRepository.save(membership);

        assertThat(membershipRepository.existsByUserIdAndStatus(700L, MembershipStatus.ACTIVE)).isTrue();
        assertThat(membershipRepository.existsByUserIdAndStatus(700L, MembershipStatus.CANCELLED)).isFalse();
        assertThat(membershipRepository.existsByUserIdAndStatus(999L, MembershipStatus.ACTIVE)).isFalse();
    }

    @Test
    @DisplayName("Should increment version on update (optimistic locking)")
    void optimisticLocking_versionIncrement() {
        MembershipPlan plan = getOrFetchPlan();
        MembershipTier tier = getOrFetchTier();

        UserMembership membership = UserMembership.builder()
                .userId(800L)
                .plan(plan)
                .tier(tier)
                .status(MembershipStatus.ACTIVE)
                .startDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusMonths(1))
                .build();
        UserMembership saved = membershipRepository.save(membership);
        Long initialVersion = saved.getVersion();

        saved.setStatus(MembershipStatus.CANCELLED);
        UserMembership updated = membershipRepository.save(saved);

        assertThat(updated.getVersion()).isGreaterThan(initialVersion);
    }

    // Helpers to load seeded reference data
    private MembershipPlan getOrFetchPlan() {
        return planRepository.findAllByActiveTrueOrderByDurationMonthsAsc().get(0);
    }

    private MembershipTier getOrFetchTier() {
        return tierRepository.findAllByActiveTrueOrderByPriorityDesc().get(0);
    }
}
