package com.membership.exception;

import org.springframework.http.HttpStatus;

public class MembershipExpiredException extends MembershipException {
    public MembershipExpiredException(String message) {
        super(message, HttpStatus.GONE);
    }
}
