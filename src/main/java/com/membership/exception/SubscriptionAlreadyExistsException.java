package com.membership.exception;

import org.springframework.http.HttpStatus;

public class SubscriptionAlreadyExistsException extends MembershipException {
    public SubscriptionAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
