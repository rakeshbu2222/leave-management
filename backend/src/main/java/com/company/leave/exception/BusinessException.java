package com.company.leave.exception;

/**
 * Thrown when a business rule is broken (e.g. not enough leave balance).
 * Mapped to HTTP 400 by GlobalExceptionHandler.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
