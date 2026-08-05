package com.fraud.engine.service;

import com.fraud.engine.dto.FraudResultDTO;
import com.fraud.engine.dto.FraudRuleEvaluation;
import com.fraud.engine.entity.FraudRule;
import com.fraud.engine.entity.FraudResult;
import com.fraud.engine.entity.Transaction;
import com.fraud.engine.enums.FraudCategory;
import com.fraud.engine.enums.ReasonCode;
import com.fraud.engine.enums.TransactionStatus;
import com.fraud.engine.exception.RuleConfigurationException;
import com.fraud.engine.repository.FraudResultRepository;
import com.fraud.engine.repository.FraudRuleRepository;
import com.fraud.engine.repository.TransactionRepository;
import com.fraud.engine.strategy.FraudRuleStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * FraudEvaluationService - Core business logic for fraud detection.
 * 
 * ARCHITECTURE:
 * -------------
 * This service implements the Strategy Pattern aggregation logic:
 * 1. Loads all enabled fraud rules ordered by priority
 * 2. Maps each rule to its corresponding Strategy implementation
 * 3. Executes strategies sequentially, collecting evaluations
 * 4. Aggregates risk scores and determines final fraud category
 * 5. Persists FraudResult and updates Transaction status
 * 
 * THREAD SAFETY:
 * --------------
 * - @Transactional ensures ACID properties during evaluation
 * - Optimistic locking (@Version on Transaction) prevents lost updates
 * - Service is stateless (no instance variables modified during execution)
 * 
 * PERFORMANCE:
 * -----------
 * - Rules are evaluated in priority order (critical rules first)
 * - Early termination possible if max risk score reached (not implemented in v1)
 * - Recent transactions loaded once and passed to all strategies
 * 
 * @see FraudRuleStrategy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FraudEvaluationService {

    private final FraudRuleRepository fraudRuleRepository;
    private final FraudResultRepository fraudResultRepository;
    private final TransactionRepository transactionRepository;
    private final List<FraudRuleStrategy> strategies;

    /**
     * Evaluates a transaction against all configured fraud rules.
     * 
     * Processing Flow:
     * 1. Load enabled rules from database (cached in production)
     * 2. Load recent transactions for velocity checks
     * 3. For each rule, find matching strategy and evaluate
     * 4. Aggregate results: sum risk scores, collect reason codes
     * 5. Determine fraud category based on cumulative score
     * 6. Save FraudResult and update Transaction status
     * 
     * @param transaction The transaction to evaluate
     * @return FraudResult containing evaluation outcome
     */
    @Transactional
    public FraudResult evaluateTransaction(Transaction transaction) {
        String txId = transaction.getTransactionId();
        log.info("Starting fraud evaluation for transaction: {}", txId);

        // Step 1: Update status to PROCESSING
        transaction.setStatus(TransactionStatus.PROCESSING);
        transactionRepository.save(transaction);

        // Step 2: Load enabled rules ordered by priority
        List<FraudRule> enabledRules = fraudRuleRepository.findAllEnabledOrderedByPriority();
        if (enabledRules.isEmpty()) {
            log.error("No fraud rules configured in the system");
            throw new RuleConfigurationException("No fraud rules are configured or enabled");
        }
        log.debug("Loaded {} enabled fraud rules for evaluation", enabledRules.size());

        // Step 3: Load recent transactions for velocity-based rules
        LocalDateTime windowStart = LocalDateTime.now().minusHours(1); // 1-hour lookback
        List<Transaction> recentTransactions = transactionRepository.findRecentTransactionsByCustomer(
                transaction.getCustomerId(), windowStart, TransactionStatus.COMPLETED);
        log.debug("Loaded {} recent transactions for customer {}", 
                recentTransactions.size(), transaction.getCustomerId());

        // Step 4: Evaluate each rule using Strategy Pattern
        List<FraudRuleEvaluation> evaluations = new ArrayList<>();
        Map<String, FraudRuleStrategy> strategyMap = strategies.stream()
                .collect(Collectors.toMap(FraudRuleStrategy::getRuleType, s -> s));

        for (FraudRule rule : enabledRules) {
            FraudRuleStrategy strategy = strategyMap.get(rule.getRuleType());
            if (strategy == null) {
                log.warn("No strategy implementation found for rule type: {}", rule.getRuleType());
                continue;
            }

            try {
                FraudRuleEvaluation evaluation = strategy.evaluate(transaction, recentTransactions, rule);
                evaluations.add(evaluation);

                if (evaluation.isTriggered()) {
                    log.info("Rule '{}' TRIGGERED for transaction {} - Risk Score: {}, Reason: {}",
                            rule.getRuleName(), txId, evaluation.getRiskScore(), evaluation.getReasonCode());
                } else {
                    log.debug("Rule '{}' passed for transaction {}", rule.getRuleName(), txId);
                }
            } catch (Exception e) {
                log.error("Error evaluating rule '{}' for transaction {}: {}", 
                        rule.getRuleName(), txId, e.getMessage(), e);
                // Continue with other rules - partial failure should not block entire evaluation
            }
        }

        // Step 5: Aggregate results
        int totalRiskScore = evaluations.stream()
                .filter(FraudRuleEvaluation::isTriggered)
                .mapToInt(FraudRuleEvaluation::getRiskScore)
                .sum();

        // Cap risk score at 100
        totalRiskScore = Math.min(totalRiskScore, 100);

        List<ReasonCode> triggeredReasons = evaluations.stream()
                .filter(FraudRuleEvaluation::isTriggered)
                .map(FraudRuleEvaluation::getReasonCode)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<String> triggeredRules = evaluations.stream()
                .filter(FraudRuleEvaluation::isTriggered)
                .map(FraudRuleEvaluation::getRuleName)
                .toList();

        // Step 6: Determine fraud category
        FraudCategory category = determineFraudCategory(totalRiskScore);

        log.info("Fraud evaluation complete for transaction {}. Category: {}, Risk Score: {}, Rules Triggered: {}",
                txId, category, totalRiskScore, triggeredRules.size());

        // Step 7: Update transaction status
        transaction.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(transaction);

        // Step 8: Save fraud result
        FraudResult fraudResult = FraudResult.builder()
                .transaction(transaction)
                .fraudCategory(category)
                .riskScore(totalRiskScore)
                .reasonCodes(triggeredReasons.stream()
                        .map(ReasonCode::getCode)
                        .collect(Collectors.joining(",")))
                .rulesTriggered(String.join(",", triggeredRules))
                .evaluatedAt(LocalDateTime.now())
                .build();

        return fraudResultRepository.save(fraudResult);
    }

    /**
     * Determines fraud category based on cumulative risk score.
     * 
     * Score Ranges:
     * - 0-30:   SAFE (normal transaction)
     * - 31-70:  SUSPICIOUS (requires manual review)
     * - 71-100: FRAUD (automatically block)
     * 
     * These thresholds are configurable and should be tuned based on
     * business risk appetite and false positive rates.
     */
    private FraudCategory determineFraudCategory(int riskScore) {
        if (riskScore <= 30) {
            return FraudCategory.SAFE;
        } else if (riskScore <= 70) {
            return FraudCategory.SUSPICIOUS;
        } else {
            return FraudCategory.FRAUD;
        }
    }

    /**
     * Maps a FraudResult entity to its DTO representation.
     */
    public FraudResultDTO mapToDTO(FraudResult fraudResult) {
        if (fraudResult == null) {
            return null;
        }

        List<String> reasonCodes = fraudResult.getReasonCodes() != null 
                ? Arrays.asList(fraudResult.getReasonCodes().split(","))
                : Collections.emptyList();

        List<String> rulesTriggered = fraudResult.getRulesTriggered() != null
                ? Arrays.asList(fraudResult.getRulesTriggered().split(","))
                : Collections.emptyList();

        return FraudResultDTO.builder()
                .category(fraudResult.getFraudCategory())
                .riskScore(fraudResult.getRiskScore())
                .reasonCodes(reasonCodes)
                .rulesTriggered(rulesTriggered)
                .evaluatedAt(fraudResult.getEvaluatedAt())
                .build();
    }
}
