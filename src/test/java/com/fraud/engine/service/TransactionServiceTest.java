package com.fraud.engine.service;

import com.fraud.engine.dto.FraudResultDTO;
import com.fraud.engine.dto.TransactionRequestDTO;
import com.fraud.engine.dto.TransactionResponseDTO;
import com.fraud.engine.entity.FraudResult;
import com.fraud.engine.entity.Transaction;
import com.fraud.engine.enums.FraudCategory;
import com.fraud.engine.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

	@Mock
	private TransactionRepository transactionRepository;

	@Mock
	private FraudEvaluationService fraudEvaluationService;

	@Mock
	private IdempotencyService idempotencyService;

	@InjectMocks
	private TransactionService transactionService;

	@Test
	void processTransaction_NewTransaction_ReturnsResult() {
		TransactionRequestDTO request = createRequest();
		Transaction transaction = createTransaction();
		FraudResult fraudResult = FraudResult.builder().fraudCategory(FraudCategory.SAFE).riskScore(10).build();

		when(idempotencyService.checkIdempotency(any())).thenReturn(Optional.empty());
		when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
		when(fraudEvaluationService.evaluateTransaction(any())).thenReturn(fraudResult);
		when(fraudEvaluationService.mapToDTO(any()))
				.thenReturn(FraudResultDTO.builder().category(FraudCategory.SAFE).riskScore(10).build());

		TransactionResponseDTO response = transactionService.processTransaction(request);

		assertNotNull(response);
		assertEquals(FraudCategory.SAFE, response.getFraudResult().getCategory());
	}

	@Test
	void processTransaction_DuplicateTransaction_ReturnsExistingResult() {
		TransactionRequestDTO request = createRequest();
		Transaction existing = createTransaction();

		when(idempotencyService.checkIdempotency(any())).thenReturn(Optional.of(existing));
		when(idempotencyService.getExistingResult(any())).thenReturn(Optional.empty());

		TransactionResponseDTO response = transactionService.processTransaction(request);

		assertNotNull(response);
		assertEquals("TXN-001-UUID", response.getTransactionId());
		verify(transactionRepository, never()).save(any());
	}

	@Test
	void getTransaction_ExistingTransaction_ReturnsResponse() {
		Transaction transaction = createTransaction();
		when(transactionRepository.findByTransactionId("TXN-001-UUID")).thenReturn(Optional.of(transaction));
		when(idempotencyService.getExistingResult(any())).thenReturn(Optional.empty());

		TransactionResponseDTO response = transactionService.getTransaction("TXN-001-UUID");

		assertNotNull(response);
		assertEquals("TXN-001-UUID", response.getTransactionId());
	}

	private TransactionRequestDTO createRequest() {
		return TransactionRequestDTO.builder().idempotencyKey(UUID.randomUUID().toString())
				.amount(new BigDecimal("2500.00")).currency("INR").cardNumber("4111111111111111")
				.merchantId("SWIGGY001").merchantName("Swiggy").customerId("CUST999999").geoLocation("Bangalore, India")
				.build();
	}

	private Transaction createTransaction() {
		return Transaction.builder().id(1L).transactionId("TXN-001-UUID").amount(new BigDecimal("2500.00"))
				.currency("INR").cardNumber("411111XXXXXX1111").merchantId("SWIGGY001").merchantName("Swiggy")
				.customerId("CUST999999").build();
	}
}