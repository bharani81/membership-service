package com.membership.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception for all domain-specific membership errors.
 * Carries an HTTP status to decouple the domain layer from HTTP concerns
 * while still enabling the global handler to map correctly.
 */
public class MembershipException extends RuntimeException {

    private final HttpStatus httpStatus;

    public MembershipException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public MembershipException(String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
