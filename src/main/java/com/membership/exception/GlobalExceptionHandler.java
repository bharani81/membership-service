package com.membership.exception;

import com.membership.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Centralized exception handler that converts all exceptions to consistent {@link ErrorResponse} objects.
 *
 * <p>Ensures:
 * <ul>
 *   <li>No stack traces leak to the client.</li>
 *   <li>All errors include a traceId for log correlation.</li>
 *   <li>HTTP status codes are semantically correct.</li>
 *   <li>Validation errors expose field-level details.</li>
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles all domain-specific membership exceptions.
     */
    @ExceptionHandler(MembershipException.class)
    public ResponseEntity<ErrorResponse> handleMembershipException(
            MembershipException ex, HttpServletRequest request) {

        String traceId = generateTraceId();
        log.warn("MembershipException [traceId={}] path={} status={} message={}",
                traceId, request.getRequestURI(), ex.getHttpStatus().value(), ex.getMessage());

        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(buildErrorResponse(ex.getHttpStatus(), ex.getMessage(), request, traceId));
    }

    /**
     * Handles optimistic locking failures — two concurrent modifications to the same entity.
     * Maps to HTTP 409 Conflict.
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailure(
            ObjectOptimisticLockingFailureException ex, HttpServletRequest request) {

        String traceId = generateTraceId();
        log.warn("Optimistic locking conflict [traceId={}] path={} entity={}",
                traceId, request.getRequestURI(), ex.getPersistentClassName());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(buildErrorResponse(HttpStatus.CONFLICT,
                        "Concurrent modification detected. The resource was modified by another request. Please retry.",
                        request, traceId));
    }

    /**
     * Handles Bean Validation failures from {@code @Valid} request bodies.
     * Returns 400 with field-level error details.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String traceId = generateTraceId();
        String fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        log.debug("Validation failure [traceId={}] path={} errors={}", traceId, request.getRequestURI(), fieldErrors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(HttpStatus.BAD_REQUEST, fieldErrors, request, traceId));
    }

    /**
     * Catch-all for unexpected exceptions. Returns 500 without exposing internals.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        String traceId = generateTraceId();
        log.error("Unhandled exception [traceId={}] path={}", traceId, request.getRequestURI(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                        "An unexpected error occurred. Please contact support with traceId: " + traceId,
                        request, traceId));
    }

    private ErrorResponse buildErrorResponse(HttpStatus status, String message,
                                              HttpServletRequest request, String traceId) {
        return new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                traceId
        );
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
