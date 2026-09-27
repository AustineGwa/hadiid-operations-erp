package com.hadiid.erp.common.exception;

/** A requested record does not exist — mapped to a 404 page by GlobalExceptionHandler. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
