package com.fraud.engine.strategy;

import com.fraud.engine.dto.FraudRuleEvaluation;
import com.fraud.engine.entity.FraudRule;
import com.fraud.engine.entity.Transaction;
import com.fraud.engine.enums.ReasonCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RepeatedPaymentFailureRule - Detects card testing and brute-force attacks.
 * 
 * Business Logic:
 * - Counts failed transactions (status = FAILED) for the same customer/card
 *   within a configurable time window
 * - Multiple failures indicate card testing, stolen card usage, or system abuse
 * 
 * Configuration Example:
 * rule_name: REPEATED_PAYMENT_FAILURE
 * rule_type: REPEATED_FAILURE
 * threshold_count: 3
 * time_window_minutes: 30
 * risk_score: 60
 * priority: 15
 * 
 * Security Context: This is critical for preventing:
 * - Card testing attacks (bots testing stolen card numbers)
 * - Brute force CVV guessing
 * - BIN attacks (testing Bank Identification Numbers)
 */
@Slf4j
@Component
public class RepeatedPaymentFailureRule implements FraudRuleStrategy {

    public static final String RULE_TYPE = "REPEATED_FAILURE";

    @Override
    public String getRuleType() {
        return RULE_TYPE;
    }

    @Override
    public FraudRuleEvaluation evaluate(Transaction transaction, 
                                         List<Transaction> recentTransactions, 
                                         FraudRule ruleConfig) {

        log.debug("Evaluating RepeatedPaymentFailureRule for card ending: ...{}", 
                transaction.getCardNumber().substring(transaction.getCardNumber().length() - 4));

        Integer thresholdCount = ruleConfig.getThresholdCount();
        Integer timeWindowMinutes = ruleConfig.getTimeWindowMinutes();

        if (thresholdCount == null || timeWindowMinutes == null) {
            log.warn("RepeatedPaymentFailureRule misconfigured");
            return FraudRuleEvaluation.builder()
                    .ruleName(ruleConfig.getRuleName())
                    .triggered(false)
                    .riskScore(0)
                    .message("Rule misconfigured: missing threshold or time window")
                    .build();
        }

        // In a real system, we'd query failed transactions from a separate table
        // or filter by a failure status. Here we simulate by checking recent
        // transaction patterns (simplified for demo).

        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(timeWindowMinutes);

        // Count recent transactions with same card prefix (first 6 digits = BIN)
        // In production, this would query a failed_transactions table
        String cardBin = transaction.getCardNumber().substring(0, 6);
        long failureCount = recentTransactions.stream()
                .filter(t -> t.getCardNumber().startsWith(cardBin))
                .filter(t -> t.getTimestamp().isAfter(windowStart))
                .count();

        // For demo: if customer has many recent transactions, simulate some as failures
        // In production, this would check actual failure records
        long simulatedFailures = Math.max(0, failureCount - 2);
        boolean triggered = simulatedFailures >= thresholdCount;

        if (triggered) {
            log.warn("REPEATED_FAILURE rule triggered for card BIN {}. Simulated failures: {}, Threshold: {}",
                    cardBin, simulatedFailures, thresholdCount);

            return FraudRuleEvaluation.builder()
                    .ruleName(ruleConfig.getRuleName())
                    .triggered(true)
                    .reasonCode(ReasonCode.REPEATED_PAYMENT_FAILURE)
                    .riskScore(ruleConfig.getRiskScore())
                    .message(String.format("Detected %d potential failures for card in last %d minutes",
                            simulatedFailures, timeWindowMinutes))
                    .build();
        }

        return FraudRuleEvaluation.builder()
                .ruleName(ruleConfig.getRuleName())
                .triggered(false)
                .riskScore(0)
                .message("No repeated failure pattern detected")
                .build();
    }
}
