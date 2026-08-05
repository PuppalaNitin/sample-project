package com.fraud.engine.service;

import com.fraud.engine.dto.FraudResultDTO;
import com.fraud.engine.dto.TransactionRequestDTO;
import com.fraud.engine.dto.TransactionResponseDTO;
import com.fraud.engine.entity.FraudResult;
import com.fraud.engine.entity.Transaction;
import com.fraud.engine.enums.TransactionStatus;
import com.fraud.engine.exception.DuplicateTransactionException;
import com.fraud.engine.exception.TransactionNotFoundException;
import com.fraud.engine.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * TransactionService - Orchestrates transaction processing and fraud evaluation.
 * 
 * RESPONSIBILITIES:
 * ----------------
 * 1. Receives validated DTO from Controller layer
 * 2. Checks idempotency to prevent duplicate processing
 * 3. Converts DTO to Entity and persists
 * 4. Delegates fraud evaluation to FraudEvaluationService
 * 5. Maps results back to DTO for API response
 * 
 * TRANSACTION MANAGEMENT:
 * -----------------------
 * - @Transactional on processTransaction ensures all-or-nothing semantics
 * - If fraud evaluation fails, transaction is rolled back
 * - Idempotency check happens outside transaction boundary (read-only)
 * 
 * CONCURRENCY:
 * -----------
 * - Multiple threads can call processTransaction simultaneously
 * - Database unique constraint on idempotency_key prevents duplicates
 * - Optimistic locking handles concurrent updates to same transaction
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final FraudEvaluationService fraudEvaluationService;
    private final IdempotencyService idempotencyService;

    /**
     * Processes a new transaction through the fraud detection pipeline.
     * 
     * @param requestDTO Validated transaction request
     * @return TransactionResponseDTO with fraud evaluation results
     * @throws DuplicateTransactionException if idempotency key already exists
     */
    @Transactional
    public TransactionResponseDTO processTransaction(TransactionRequestDTO requestDTO) {
        log.info("Processing transaction request with idempotency key: {}", requestDTO.getIdempotencyKey());

        // Step 1: Idempotency check (read-only, no transaction needed)
        Optional<Transaction> existingTx = idempotencyService.checkIdempotency(requestDTO.getIdempotencyKey());
        if (existingTx.isPresent()) {
            log.info("Returning existing result for idempotent transaction: {}", requestDTO.getIdempotencyKey());
            return buildResponseFromExistingTransaction(existingTx.get());
        }

        // Step 2: Create and save transaction entity
        Transaction transaction = mapToEntity(requestDTO);
        transaction = transactionRepository.save(transaction);
        log.debug("Transaction saved with ID: {}", transaction.getId());

        // Step 3: Evaluate fraud rules
        FraudResult fraudResult = fraudEvaluationService.evaluateTransaction(transaction);
        log.info("Transaction {} evaluated as {} with risk score {}",
                transaction.getTransactionId(), 
                fraudResult.getFraudCategory(), 
                fraudResult.getRiskScore());

        // Step 4: Build and return response
        return buildResponse(transaction, fraudResult);
    }

    /**
     * Retrieves a transaction by its business identifier.
     * 
     * @param transactionId The business transaction ID (UUID)
     * @return TransactionResponseDTO with fraud results
     * @throws TransactionNotFoundException if not found
     */
    @Transactional(readOnly = true)
    public TransactionResponseDTO getTransaction(String transactionId) {
        log.debug("Fetching transaction: {}", transactionId);

        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));

        return buildResponseFromExistingTransaction(transaction);
    }

    /**
     * Converts request DTO to entity. Masks card number for storage.
     */
    private Transaction mapToEntity(TransactionRequestDTO dto) {
        return Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .idempotencyKey(dto.getIdempotencyKey())
                .amount(dto.getAmount())
                .currency(dto.getCurrency().toUpperCase())
                .cardNumber(maskCardNumber(dto.getCardNumber()))
                .merchantId(dto.getMerchantId())
                .merchantName(dto.getMerchantName())
                .customerId(dto.getCustomerId())
                .ipAddress(dto.getIpAddress())
                .geoLocation(dto.getGeoLocation())
                .deviceId(dto.getDeviceId())
                .timestamp(dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now())
                .status(TransactionStatus.PENDING)
                .build();
    }

    /**
     * Masks card number for PCI DSS compliance.
     * Stores only first 6 (BIN) and last 4 digits.
     * Example: 123456XXXXXX7890
     */
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 10) {
            return cardNumber;
        }
        return cardNumber.substring(0, 6) + 
               "X".repeat(cardNumber.length() - 10) + 
               cardNumber.substring(cardNumber.length() - 4);
    }

    /**
     * Builds response for a newly processed transaction.
     */
    private TransactionResponseDTO buildResponse(Transaction transaction, FraudResult fraudResult) {
        return TransactionResponseDTO.builder()
                .transactionId(transaction.getTransactionId())
                .idempotencyKey(transaction.getIdempotencyKey())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .cardNumberMasked(transaction.getCardNumber())
                .merchantName(transaction.getMerchantName())
                .customerId(transaction.getCustomerId())
                .geoLocation(transaction.getGeoLocation())
                .status(transaction.getStatus())
                .timestamp(transaction.getTimestamp())
                .processedAt(LocalDateTime.now())
                .fraudResult(fraudEvaluationService.mapToDTO(fraudResult))
                .build();
    }

    /**
     * Builds response for an existing (idempotent) transaction.
     */
    private TransactionResponseDTO buildResponseFromExistingTransaction(Transaction transaction) {
        FraudResultDTO fraudResultDTO = idempotencyService.getExistingResult(transaction)
                .map(fraudEvaluationService::mapToDTO)
                .orElse(null);

        return TransactionResponseDTO.builder()
                .transactionId(transaction.getTransactionId())
                .idempotencyKey(transaction.getIdempotencyKey())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .cardNumberMasked(transaction.getCardNumber())
                .merchantName(transaction.getMerchantName())
                .customerId(transaction.getCustomerId())
                .geoLocation(transaction.getGeoLocation())
                .status(transaction.getStatus())
                .timestamp(transaction.getTimestamp())
                .processedAt(transaction.getUpdatedAt())
                .fraudResult(fraudResultDTO)
                .build();
    }
}
