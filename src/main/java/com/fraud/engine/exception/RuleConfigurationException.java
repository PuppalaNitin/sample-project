package com.fraud.engine.exception;

/**
 * Thrown when a fraud rule configuration is missing or invalid.
 * HTTP Status: 500 INTERNAL SERVER ERROR
 */
public class RuleConfigurationException extends FraudEngineException {

    private static final String ERROR_CODE = "ERR_RULE_CONFIG";

    public RuleConfigurationException(String message) {
        super(ERROR_CODE, message);
    }
}
