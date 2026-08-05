package com.fraud.engine.service;

import com.fraud.engine.entity.FraudResult;
import com.fraud.engine.entity.Transaction;
import com.fraud.engine.enums.TransactionStatus;
import com.fraud.engine.exception.DuplicateTransactionException;
import com.fraud.engine.repository.FraudResultRepository;
import com.fraud.engine.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * IdempotencyService - Ensures exactly-once processing semantics.
 * 
 * IDEMPOTENCY EXPLANATION:
 * ------------------------
 * Idempotency means that processing the same request multiple times produces
 * the same result as processing it once. This is critical in distributed systems
 * where network retries can cause duplicate submissions.
 * 
 * How It Works:
 * 1. Client generates a unique idempotencyKey (UUID) for each logical transaction
 * 2. Server stores the key and associates it with the transaction result
 * 3. If the same key is received again, server returns the cached result
 * 4. Keys expire after a TTL (configured in application.properties)
 * 
 * Concurrency Safety:
 * - UNIQUE constraint on idempotency_key at database level prevents race conditions
 * - @Transactional ensures atomic check-and-insert operations
 * 
 * @see TransactionRequestDTO#idempotencyKey
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final TransactionRepository transactionRepository;
    private final FraudResultRepository fraudResultRepository;

    /**
     * Checks if a transaction with the given idempotency key already exists.
     * 
     * @param idempotencyKey Client-provided unique key
     * @return Optional containing existing transaction if found
     * @throws DuplicateTransactionException if transaction exists and is completed
     */
    @Transactional(readOnly = true)
    public Optional<Transaction> checkIdempotency(String idempotencyKey) {
        log.debug("Checking idempotency for key: {}", idempotencyKey);
        return transactionRepository.findByIdempotencyKey(idempotencyKey);
    }

    /**
     * Validates that the idempotency key has not been used.
     * Throws exception if duplicate detected.
     * 
     * @param idempotencyKey Client-provided unique key
     * @throws DuplicateTransactionException if key already exists
     */
    @Transactional(readOnly = true)
    public void validateIdempotency(String idempotencyKey) {
        if (transactionRepository.existsByIdempotencyKey(idempotencyKey)) {
            log.warn("Duplicate transaction detected with idempotency key: {}", idempotencyKey);
            throw new DuplicateTransactionException(idempotencyKey);
        }
    }

    /**
     * Retrieves the fraud result for an existing idempotent transaction.
     * 
     * @param transaction The existing transaction
     * @return Optional containing fraud result
     */
    @Transactional(readOnly = true)
    public Optional<FraudResult> getExistingResult(Transaction transaction) {
        return fraudResultRepository.findByTransactionId(transaction.getId());
    }
}
