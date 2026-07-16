package com.membership;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Membership Service.
 *
 * <p>This service provides a production-ready membership management system with:
 * <ul>
 *   <li>Clean layered architecture (Controller → Service → Domain → Repository)</li>
 *   <li>Strategy Pattern for configurable tier evaluation</li>
 *   <li>Factory Pattern for extensible membership actions</li>
 *   <li>Optimistic locking for concurrent request safety</li>
 *   <li>Flyway-managed database migrations</li>
 * </ul>
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class MembershipServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MembershipServiceApplication.class, args);
    }
}
