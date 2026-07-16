package com.membership.exception;

import org.springframework.http.HttpStatus;

public class MembershipNotFoundException extends MembershipException {
    public MembershipNotFoundException(Long membershipId) {
        super("Membership not found with id: " + membershipId, HttpStatus.NOT_FOUND);
    }
    public MembershipNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
