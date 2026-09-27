package com.hadiid.erp.common.exception;

/**
 * A business/validation rule was violated (invalid stage transition, duplicate
 * key, missing reason on a correction, etc). Mapped to a 400-style flash message
 * back onto the originating form, never a raw stack trace.
 */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
