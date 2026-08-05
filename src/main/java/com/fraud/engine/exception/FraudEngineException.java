package com.fraud.engine.exception;

/**
 * Base exception for all fraud engine business logic errors.
 * 
 * Provides errorCode field for programmatic error handling and client-side mapping.
 */
public class FraudEngineException extends RuntimeException {

    private final String errorCode;

    public FraudEngineException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public FraudEngineException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
