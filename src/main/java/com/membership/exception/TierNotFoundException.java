package com.membership.exception;

import org.springframework.http.HttpStatus;

public class TierNotFoundException extends MembershipException {
    public TierNotFoundException(Long tierId) {
        super("Membership tier not found with id: " + tierId, HttpStatus.NOT_FOUND);
    }
}
