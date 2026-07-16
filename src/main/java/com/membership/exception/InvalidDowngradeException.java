package com.membership.exception;

import org.springframework.http.HttpStatus;

public class InvalidDowngradeException extends MembershipException {
    public InvalidDowngradeException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
