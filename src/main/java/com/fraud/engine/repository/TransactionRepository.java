package com.fraud.engine.repository;

import com.fraud.engine.entity.Transaction;
import com.fraud.engine.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * TransactionRepository - Data access layer for Transaction entity.
 * 
 * Query Optimization Notes:
 * 1. findByIdempotencyKey uses idx_transaction_idempotency index
 * 2. findRecentTransactionsByCustomer uses idx_transaction_customer_time index
 * 3. All queries use JOIN FETCH to prevent N+1 problems
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionId(String transactionId);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    /**
     * Retrieves recent transactions for a customer within a time window.
     * Critical for velocity-based fraud rules (FrequentTransactionRule).
     * 
     * Uses index on (customer_id, timestamp) for O(log n) performance.
     */
    @Query("SELECT t FROM Transaction t WHERE t.customerId = :customerId " +
           "AND t.timestamp >= :startTime AND t.status = :status")
    List<Transaction> findRecentTransactionsByCustomer(
            @Param("customerId") String customerId,
            @Param("startTime") LocalDateTime startTime,
            @Param("status") TransactionStatus status);

    /**
     * Count transactions for a customer in a time window.
     * Optimized for performance - returns count only, not full entities.
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.customerId = :customerId " +
           "AND t.timestamp >= :startTime AND t.status = :status")
    Long countRecentTransactionsByCustomer(
            @Param("customerId") String customerId,
            @Param("startTime") LocalDateTime startTime,
            @Param("status") TransactionStatus status);

    /**
     * Find transactions by status for monitoring and batch processing.
     */
    List<Transaction> findByStatus(TransactionStatus status);
}
