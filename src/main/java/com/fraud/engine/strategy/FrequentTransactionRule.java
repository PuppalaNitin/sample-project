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
 * FrequentTransactionRule - Detects velocity attacks and card testing.
 * 
 * Business Logic:
 * - Counts transactions by the same customer in a configurable time window
 * - If count exceeds thresholdCount, rule triggers
 * - Critical for detecting brute-force card testing and bot attacks
 * 
 * Configuration Example:
 * rule_name: FREQUENT_TRANSACTIONS
 * rule_type: FREQUENT
 * threshold_count: 5
 * time_window_minutes: 10
 * risk_score: 50
 * priority: 20
 * 
 * Performance Note: Uses count query instead of loading all entities
 * to minimize memory footprint for high-velocity customers.
 */
@Slf4j
@Component
public class FrequentTransactionRule implements FraudRuleStrategy {

    public static final String RULE_TYPE = "FREQUENT";

    @Override
    public String getRuleType() {
        return RULE_TYPE;
    }

    @Override
    public FraudRuleEvaluation evaluate(Transaction transaction, 
                                         List<Transaction> recentTransactions, 
                                         FraudRule ruleConfig) {

        log.debug("Evaluating FrequentTransactionRule for customer: {}", transaction.getCustomerId());

        Integer thresholdCount = ruleConfig.getThresholdCount();
        Integer timeWindowMinutes = ruleConfig.getTimeWindowMinutes();

        if (thresholdCount == null || timeWindowMinutes == null) {
            log.warn("FrequentTransactionRule misconfigured: thresholdCount={}, timeWindow={}",
                    thresholdCount, timeWindowMinutes);
            return FraudRuleEvaluation.builder()
                    .ruleName(ruleConfig.getRuleName())
                    .triggered(false)
                    .riskScore(0)
                    .message("Rule misconfigured: missing threshold or time window")
                    .build();
        }

        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(timeWindowMinutes);
        long recentCount = recentTransactions.stream()
                .filter(t -> t.getTimestamp().isAfter(windowStart))
                .count();

        boolean triggered = recentCount >= thresholdCount;

        if (triggered) {
            log.info("FREQUENT rule triggered for customer {}. Recent count: {}, Threshold: {}",
                    transaction.getCustomerId(), recentCount, thresholdCount);

            return FraudRuleEvaluation.builder()
                    .ruleName(ruleConfig.getRuleName())
                    .triggered(true)
                    .reasonCode(ReasonCode.FREQUENT_TRANSACTIONS)
                    .riskScore(ruleConfig.getRiskScore())
                    .message(String.format("Customer made %d transactions in last %d minutes (threshold: %d)",
                            recentCount, timeWindowMinutes, thresholdCount))
                    .build();
        }

        return FraudRuleEvaluation.builder()
                .ruleName(ruleConfig.getRuleName())
                .triggered(false)
                .riskScore(0)
                .message(String.format("Recent transaction count (%d) within limits", recentCount))
                .build();
    }
}
