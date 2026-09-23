package com.company.leave.exception;

/** Thrown when a record is not found in DB. Mapped to HTTP 404 by GlobalExceptionHandler. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
