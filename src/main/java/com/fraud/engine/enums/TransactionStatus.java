package com.fraud.engine.enums;

/**
 * Represents the lifecycle status of a transaction in the fraud detection pipeline.
 * 
 * PENDING    - Transaction received, awaiting fraud evaluation
 * PROCESSING - Fraud rules are being evaluated
 * COMPLETED  - Evaluation finished, result stored
 * FAILED     - Processing error occurred
 */
public enum TransactionStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}
