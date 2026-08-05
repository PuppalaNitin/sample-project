package com.fraud.engine.enums;

/**
 * Standardized reason codes for fraud rule triggers.
 * Each code maps to a specific fraud detection scenario for audit and reporting.
 */
public enum ReasonCode {
    HIGH_VALUE_TRANSACTION("F001", "Transaction amount exceeds configured threshold"),
    FREQUENT_TRANSACTIONS("F002", "Unusual frequency of transactions detected"),
    GEO_LOCATION_ANOMALY("F003", "Transaction location inconsistent with user profile"),
    REPEATED_PAYMENT_FAILURE("F004", "Multiple payment failures in short time window"),
    VELOCITY_CHECK_FAILED("F005", "Transaction velocity exceeds acceptable limits"),
    MERCHANT_RISK_HIGH("F006", "Merchant identified as high-risk"),
    CARD_NOT_PRESENT("F007", "Card-not-present transaction flagged"),
    SUSPICIOUS_DEVICE("F008", "Device fingerprint matches known fraud pattern");

    private final String code;
    private final String description;

    ReasonCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
