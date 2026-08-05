package com.fraud.engine.strategy;

import com.fraud.engine.dto.FraudRuleEvaluation;
import com.fraud.engine.entity.FraudRule;
import com.fraud.engine.entity.Transaction;
import com.fraud.engine.enums.ReasonCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FraudRuleStrategyTest {

    @Test
    void highValueTransactionRule_Triggered_WhenAmountExceedsThreshold() {
        HighValueTransactionRule rule = new HighValueTransactionRule();

        Transaction transaction = Transaction.builder()
                .amount(new BigDecimal("75000.00"))
                .currency("INR")
                .build();

        FraudRule config = FraudRule.builder()
                .ruleName("HIGH_VALUE_TRANSACTION")
                .thresholdAmount(new BigDecimal("50000.00"))
                .riskScore(40)
                .build();

        FraudRuleEvaluation result = rule.evaluate(transaction, Collections.emptyList(), config);

        assertTrue(result.isTriggered());
        assertEquals(ReasonCode.HIGH_VALUE_TRANSACTION, result.getReasonCode());
        assertEquals(40, result.getRiskScore());
    }

    @Test
    void highValueTransactionRule_NotTriggered_WhenAmountBelowThreshold() {
        HighValueTransactionRule rule = new HighValueTransactionRule();

        Transaction transaction = Transaction.builder()
                .amount(new BigDecimal("10000.00"))
                .currency("INR")
                .build();

        FraudRule config = FraudRule.builder()
                .ruleName("HIGH_VALUE_TRANSACTION")
                .thresholdAmount(new BigDecimal("50000.00"))
                .riskScore(40)
                .build();

        FraudRuleEvaluation result = rule.evaluate(transaction, Collections.emptyList(), config);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getRiskScore());
    }

    @Test
    void frequentTransactionRule_Triggered_WhenCountExceedsThreshold() {
        FrequentTransactionRule rule = new FrequentTransactionRule();

        Transaction transaction = Transaction.builder()
                .customerId("CUST001")
                .build();

        List<Transaction> recent = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            recent.add(Transaction.builder()
                    .customerId("CUST001")
                    .timestamp(LocalDateTime.now().minusMinutes(i))
                    .build());
        }

        FraudRule config = FraudRule.builder()
                .ruleName("FREQUENT_TRANSACTIONS")
                .thresholdCount(5)
                .timeWindowMinutes(10)
                .riskScore(50)
                .build();

        FraudRuleEvaluation result = rule.evaluate(transaction, recent, config);

        assertTrue(result.isTriggered());
        assertEquals(ReasonCode.FREQUENT_TRANSACTIONS, result.getReasonCode());
    }

    @Test
    void geoLocationAnomalyRule_Triggered_ForHighRiskCountry() {
        GeoLocationAnomalyRule rule = new GeoLocationAnomalyRule();

        Transaction transaction = Transaction.builder()
                .geoLocation("Lagos, Nigeria")
                .build();

        FraudRule config = FraudRule.builder()
                .ruleName("GEO_LOCATION_ANOMALY")
                .riskScore(35)
                .build();

        FraudRuleEvaluation result = rule.evaluate(transaction, Collections.emptyList(), config);

        assertTrue(result.isTriggered());
        assertEquals(ReasonCode.GEO_LOCATION_ANOMALY, result.getReasonCode());
    }

    @Test
    void geoLocationAnomalyRule_NotTriggered_ForIndianCity() {
        GeoLocationAnomalyRule rule = new GeoLocationAnomalyRule();

        Transaction transaction = Transaction.builder()
                .geoLocation("Mumbai, India")
                .build();

        FraudRule config = FraudRule.builder()
                .ruleName("GEO_LOCATION_ANOMALY")
                .riskScore(35)
                .build();

        FraudRuleEvaluation result = rule.evaluate(transaction, Collections.emptyList(), config);

        assertFalse(result.isTriggered());
    }

    @Test
    void repeatedPaymentFailureRule_NotTriggered_WhenNoFailures() {
        RepeatedPaymentFailureRule rule = new RepeatedPaymentFailureRule();

        Transaction transaction = Transaction.builder()
                .cardNumber("4111111111111111")
                .build();

        FraudRule config = FraudRule.builder()
                .ruleName("REPEATED_PAYMENT_FAILURE")
                .thresholdCount(3)
                .timeWindowMinutes(30)
                .riskScore(60)
                .build();

        FraudRuleEvaluation result = rule.evaluate(transaction, Collections.emptyList(), config);

        assertFalse(result.isTriggered());
    }
}
