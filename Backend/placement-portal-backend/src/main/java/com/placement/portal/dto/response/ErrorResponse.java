package com.placement.portal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standardised error payload returned by {@link com.placement.portal.exception.GlobalExceptionHandler}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /** HTTP status code (e.g. 400, 401, 404). */
    private int status;

    /** Short error classification (e.g. {@code "Bad Request"}, {@code "Not Found"}). */
    private String error;

    /** Detailed, human-readable error description. */
    private String message;

    /** UTC timestamp of when the error occurred. */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
