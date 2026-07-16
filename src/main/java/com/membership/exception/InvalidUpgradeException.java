package com.membership.exception;

import org.springframework.http.HttpStatus;

public class InvalidUpgradeException extends MembershipException {
    public InvalidUpgradeException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
