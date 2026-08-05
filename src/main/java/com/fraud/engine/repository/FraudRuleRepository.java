package com.fraud.engine.repository;

import com.fraud.engine.entity.FraudRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * FraudRuleRepository - Data access layer for fraud rule configurations.
 * 
 * Rules are loaded at startup and cached. Runtime modifications require
 * cache eviction (not implemented in v1, reload via actuator endpoint).
 */
@Repository
public interface FraudRuleRepository extends JpaRepository<FraudRule, Long> {

    Optional<FraudRule> findByRuleName(String ruleName);

    /**
     * Load all enabled rules ordered by priority (ascending).
     * Lower priority number = higher precedence in evaluation.
     */
    @Query("SELECT fr FROM FraudRule fr WHERE fr.enabled = true ORDER BY fr.priority ASC")
    List<FraudRule> findAllEnabledOrderedByPriority();

    /**
     * Find rules by type for strategy mapping.
     */
    @Query("SELECT fr FROM FraudRule fr WHERE fr.ruleType = :ruleType AND fr.enabled = true")
    List<FraudRule> findByRuleType(@Param("ruleType") String ruleType);
}
