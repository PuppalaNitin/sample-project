package com.fraud.engine.exception;

/**
 * Thrown when a requested transaction cannot be found in the database.
 * HTTP Status: 404 NOT FOUND
 */
public class TransactionNotFoundException extends FraudEngineException {

    private static final String ERROR_CODE = "ERR_TRANSACTION_NOT_FOUND";

    public TransactionNotFoundException(String transactionId) {
        super(ERROR_CODE, "Transaction not found with ID: " + transactionId);
    }
}
