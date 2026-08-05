package com.fraud.engine.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * FraudRule Entity - Configurable fraud detection rule definition.
 * 
 * This entity drives the Strategy Pattern implementation. Each rule record
 * corresponds to a concrete strategy implementation in the codebase.
 * 
 * The priority field determines execution order (lower = higher priority).
 * The enabled flag allows runtime toggling without code deployment.
 * 
 * Extensibility: Adding a new rule requires:
 * 1. Insert a row in this table
 * 2. Create a class implementing FraudRuleStrategy
 * 3. Register the strategy in FraudEvaluationService
 */
@Entity
@Table(name = "fraud_rules", indexes = {
    @Index(name = "idx_fraud_rule_priority", columnList = "priority"),
    @Index(name = "idx_fraud_rule_enabled", columnList = "enabled")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "rule_name", nullable = false, unique = true, length = 64)
    private String ruleName;

    @Column(name = "rule_type", nullable = false, length = 64)
    private String ruleType;

    @Column(name = "priority", nullable = false)
    @Builder.Default
    private Integer priority = 100;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "threshold_amount", precision = 15, scale = 2)
    private BigDecimal thresholdAmount;

    @Column(name = "threshold_count")
    private Integer thresholdCount;

    @Column(name = "time_window_minutes")
    private Integer timeWindowMinutes;

    @Column(name = "risk_score", nullable = false)
    @Builder.Default
    private Integer riskScore = 25;

    @Column(name = "description", length = 512)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
