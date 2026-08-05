package com.fraud.engine.controller;

import com.fraud.engine.dto.FraudResultDTO;
import com.fraud.engine.dto.TransactionRequestDTO;
import com.fraud.engine.dto.TransactionResponseDTO;
import com.fraud.engine.enums.FraudCategory;
import com.fraud.engine.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FraudController.class)
class FraudControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private TransactionService transactionService;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void evaluateTransaction_ValidRequest_ReturnsOk() throws Exception {
		TransactionRequestDTO request = TransactionRequestDTO.builder().idempotencyKey("test-key-123")
				.amount(new BigDecimal("1500.00")).currency("INR").cardNumber("4111111111111111")
				.merchantId("FLIPKART001").merchantName("Flipkart").customerId("CUST123456")
				.geoLocation("Mumbai, India").build();

		// FIX: Add fraudResult so controller returns 200 OK instead of 202 ACCEPTED
		TransactionResponseDTO response = TransactionResponseDTO.builder().transactionId("tx-123-uuid")
				.fraudResult(FraudResultDTO.builder().category(FraudCategory.SAFE).riskScore(10).build()).build();

		when(transactionService.processTransaction(any())).thenReturn(response);

		mockMvc.perform(post("/api/v1/fraud/evaluate").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
	}

	@Test
	void evaluateTransaction_InvalidAmount_ReturnsBadRequest() throws Exception {
		TransactionRequestDTO request = TransactionRequestDTO.builder().idempotencyKey("test-key-456")
				.amount(new BigDecimal("-100.00")).currency("INR").cardNumber("4111111111111111")
				.merchantId("AMAZON001").merchantName("Amazon India").customerId("CUST789012").build();

		mockMvc.perform(post("/api/v1/fraud/evaluate").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());
	}

	@Test
	void getTransaction_ValidId_ReturnsOk() throws Exception {
		TransactionResponseDTO response = TransactionResponseDTO.builder().transactionId("TXN-001-UUID").build();

		when(transactionService.getTransaction("TXN-001-UUID")).thenReturn(response);

		mockMvc.perform(get("/api/v1/fraud/transactions/TXN-001-UUID")).andExpect(status().isOk())
				.andExpect(jsonPath("$.transactionId").value("TXN-001-UUID"));
	}
}