package com.fraud.engine.entity;

import com.fraud.engine.enums.FraudCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * FraudResult Entity - Stores the outcome of fraud evaluation for a transaction.
 * 
 * One-to-One relationship with Transaction. The reasonCodes field stores
 * comma-separated ReasonCode values for audit trails.
 * 
 * The riskScore is cumulative (0-100) across all triggered rules.
 */
@Entity
@Table(name = "fraud_results", indexes = {
    @Index(name = "idx_fraud_result_transaction", columnList = "transaction_id", unique = true),
    @Index(name = "idx_fraud_result_category", columnList = "fraud_category"),
    @Index(name = "idx_fraud_result_evaluated", columnList = "evaluated_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false, unique = true)
    private Transaction transaction;

    @Enumerated(EnumType.STRING)
    @Column(name = "fraud_category", nullable = false, length = 20)
    private FraudCategory fraudCategory;

    @Column(name = "reason_codes", length = 256)
    private String reasonCodes;

    @Column(name = "risk_score", nullable = false)
    @Builder.Default
    private Integer riskScore = 0;

    @Column(name = "rules_triggered", length = 512)
    private String rulesTriggered;

    @Column(name = "evaluated_at", nullable = false)
    private LocalDateTime evaluatedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
