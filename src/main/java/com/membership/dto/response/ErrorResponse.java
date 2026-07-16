package com.membership.dto.response;

import java.time.Instant;

/**
 * Standardized error response returned by the global exception handler.
 * Consistent format ensures API consumers can reliably parse errors.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String traceId
) {}
