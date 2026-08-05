package com.fraud.engine.entity;

import com.fraud.engine.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction Entity - Core domain object representing a financial transaction.
 * 
 * Design Decisions:
 * 1. @Version enables optimistic locking for concurrency-safe updates
 * 2. idempotencyKey ensures exactly-once processing semantics
 * 3. transactionId is the business identifier (UUID) separate from DB primary key
 * 4. cardNumber is stored masked for PCI DSS compliance
 * 
 * Indexes:
 * - idx_transaction_idempotency: Fast duplicate detection
 * - idx_transaction_customer_time: Time-window queries for velocity checks
 * - idx_transaction_status: Filtering by processing status
 */
@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transaction_idempotency", columnList = "idempotency_key", unique = true),
    @Index(name = "idx_transaction_customer_time", columnList = "customer_id, timestamp"),
    @Index(name = "idx_transaction_status", columnList = "status"),
    @Index(name = "idx_transaction_merchant", columnList = "merchant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"cardNumber"})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "transaction_id", nullable = false, unique = true, length = 64)
    private String transactionId;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 128)
    private String idempotencyKey;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "card_number", nullable = false, length = 19)
    private String cardNumber;

    @Column(name = "merchant_id", nullable = false, length = 64)
    private String merchantId;

    @Column(name = "merchant_name", nullable = false, length = 128)
    private String merchantName;

    @Column(name = "customer_id", nullable = false, length = 64)
    private String customerId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "geo_location", length = 128)
    private String geoLocation;

    @Column(name = "device_id", length = 64)
    private String deviceId;

    @Column(name = "transaction_timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Version
    @Column(name = "version")
    @Builder.Default
    private Long version = 0L;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
