package com.fraud.engine.strategy;

import com.fraud.engine.dto.FraudRuleEvaluation;
import com.fraud.engine.entity.FraudRule;
import com.fraud.engine.entity.Transaction;
import com.fraud.engine.enums.ReasonCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * HighValueTransactionRule - Flags transactions exceeding a monetary threshold.
 * 
 * Business Logic:
 * - Compares transaction amount against ruleConfig.thresholdAmount
 * - If amount > threshold, rule triggers with configured risk score
 * - Used to detect unusually large purchases, potential account takeover
 * 
 * Configuration Example:
 * rule_name: HIGH_VALUE_TRANSACTION
 * rule_type: HIGH_VALUE
 * threshold_amount: 50000.00
 * risk_score: 40
 * priority: 10
 * 
 * Indian Context: Threshold of INR 50,000 aligns with RBI reporting requirements
 * for high-value transactions in digital payments.
 */
@Slf4j
@Component
public class HighValueTransactionRule implements FraudRuleStrategy {

    public static final String RULE_TYPE = "HIGH_VALUE";

    @Override
    public String getRuleType() {
        return RULE_TYPE;
    }

    @Override
    public FraudRuleEvaluation evaluate(Transaction transaction, 
                                         List<Transaction> recentTransactions, 
                                         FraudRule ruleConfig) {

        log.debug("Evaluating HighValueTransactionRule for transaction: {}", transaction.getTransactionId());

        BigDecimal threshold = ruleConfig.getThresholdAmount();
        if (threshold == null) {
            log.warn("HighValueTransactionRule configured without threshold amount");
            return FraudRuleEvaluation.builder()
                    .ruleName(ruleConfig.getRuleName())
                    .triggered(false)
                    .riskScore(0)
                    .message("Rule misconfigured: threshold amount missing")
                    .build();
        }

        boolean triggered = transaction.getAmount().compareTo(threshold) > 0;

        if (triggered) {
            log.info("HIGH_VALUE rule triggered for transaction {}. Amount: {}, Threshold: {}",
                    transaction.getTransactionId(), transaction.getAmount(), threshold);

            return FraudRuleEvaluation.builder()
                    .ruleName(ruleConfig.getRuleName())
                    .triggered(true)
                    .reasonCode(ReasonCode.HIGH_VALUE_TRANSACTION)
                    .riskScore(ruleConfig.getRiskScore())
                    .message(String.format("Transaction amount %s exceeds threshold %s %s",
                            transaction.getAmount(), threshold, transaction.getCurrency()))
                    .build();
        }

        return FraudRuleEvaluation.builder()
                .ruleName(ruleConfig.getRuleName())
                .triggered(false)
                .riskScore(0)
                .message("Transaction amount within acceptable limits")
                .build();
    }
}
