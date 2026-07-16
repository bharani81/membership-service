package com.membership.exception;

import org.springframework.http.HttpStatus;

public class PlanNotFoundException extends MembershipException {
    public PlanNotFoundException(Long planId) {
        super("Membership plan not found with id: " + planId, HttpStatus.NOT_FOUND);
    }
}
