package com.fraud.engine.exception;

/**
 * Thrown when a transaction with the same idempotency key is already processed.
 * 
 * This ensures exactly-once processing semantics for the fraud detection pipeline.
 * HTTP Status: 409 CONFLICT
 */
public class DuplicateTransactionException extends FraudEngineException {

    private static final String ERROR_CODE = "ERR_DUPLICATE_TRANSACTION";

    public DuplicateTransactionException(String idempotencyKey) {
        super(ERROR_CODE, "Transaction with idempotency key '" + idempotencyKey + "' already exists");
    }
}
