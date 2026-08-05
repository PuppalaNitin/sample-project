package com.fraud.engine.strategy;

import com.fraud.engine.dto.FraudRuleEvaluation;
import com.fraud.engine.entity.FraudRule;
import com.fraud.engine.entity.Transaction;

import java.util.List;

/**
 * FraudRuleStrategy - Strategy Pattern interface for fraud detection rules.
 * 
 * DESIGN PATTERN EXPLANATION (Strategy Pattern):
 * ------------------------------------------------
 * The Strategy Pattern defines a family of algorithms (fraud rules), encapsulates each one,
 * and makes them interchangeable. This allows the fraud engine to vary its behavior independently
 * from clients that use it.
 * 
 * Why Strategy Pattern for Fraud Detection?
 * 1. EXTENSIBILITY: New rules can be added without modifying existing code (Open/Closed Principle)
 * 2. TESTABILITY: Each rule can be unit tested in isolation
 * 3. CONFIGURABILITY: Rules can be enabled/disabled via database configuration
 * 4. PRIORITIZATION: Rules execute in priority order, allowing critical checks first
 * 5. COMPOSABILITY: Results from multiple rules are aggregated for final decision
 * 
 * Implementation:
 * - Each concrete strategy implements this interface
 * - Spring collects all beans implementing this interface via @Autowired List<FraudRuleStrategy>
 * - FraudEvaluationService iterates strategies and aggregates results
 * 
 * @see FraudEvaluationService
 */
public interface FraudRuleStrategy {

    /**
     * Returns the unique rule type identifier.
     * Must match the rule_type column in the fraud_rules table.
     */
    String getRuleType();

    /**
     * Evaluates a transaction against this fraud rule.
     * 
     * @param transaction The transaction under evaluation
     * @param recentTransactions Recent transactions by the same customer (for velocity rules)
     * @param ruleConfig The database configuration for this rule (thresholds, etc.)
     * @return FraudRuleEvaluation containing trigger status and risk details
     */
    FraudRuleEvaluation evaluate(Transaction transaction, 
                                  List<Transaction> recentTransactions, 
                                  FraudRule ruleConfig);
}
