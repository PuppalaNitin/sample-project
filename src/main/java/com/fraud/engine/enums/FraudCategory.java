package com.fraud.engine.enums;

/**
 * Fraud categorization based on cumulative risk score from rule evaluation.
 * 
 * SAFE       - Risk score 0-30, no rules triggered or low risk
 * SUSPICIOUS - Risk score 31-70, some rules triggered, requires review
 * FRAUD      - Risk score 71-100, critical rules triggered, block transaction
 */
public enum FraudCategory {
    SAFE,
    SUSPICIOUS,
    FRAUD
}
