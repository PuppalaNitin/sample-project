package com.fraud.engine.controller;

import com.fraud.engine.dto.TransactionRequestDTO;
import com.fraud.engine.dto.TransactionResponseDTO;
import com.fraud.engine.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * FraudController - REST API entry point for fraud detection.
 *
 * DESIGN PRINCIPLES:
 * 1. Thin Controller: Delegates all business logic to Service layer
 * 2. Validation: Uses @Valid for automatic DTO validation
 * 3. Idempotency: Relies on client-provided idempotencyKey
 * 4. Documentation: OpenAPI annotations for auto-generated Swagger docs
 *
 * API Endpoints:
 * POST /api/v1/fraud/evaluate - Submit transaction for fraud evaluation
 * GET  /api/v1/fraud/transactions/{transactionId} - Retrieve transaction result
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/fraud")
@RequiredArgsConstructor
@Tag(name = "Fraud Detection", description = "Transaction fraud evaluation APIs")
public class FraudController {

    private final TransactionService transactionService;

    @PostMapping("/evaluate")
    @Operation(summary = "Evaluate transaction for fraud",
               description = "Submits a transaction to the fraud detection engine. Returns SAFE, SUSPICIOUS, or FRAUD classification.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction evaluated successfully",
                     content = @Content(schema = @Schema(implementation = TransactionResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request payload"),
        @ApiResponse(responseCode = "409", description = "Duplicate idempotency key"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<TransactionResponseDTO> evaluateTransaction(
            @Valid @RequestBody TransactionRequestDTO requestDTO) {

        log.info("Received fraud evaluation request for merchant: {}", requestDTO.getMerchantName());

        TransactionResponseDTO response = transactionService.processTransaction(requestDTO);

        HttpStatus status = response.getFraudResult() != null &&
                           response.getFraudResult().getCategory() != null
                ? HttpStatus.OK
                : HttpStatus.ACCEPTED;

        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/transactions/{transactionId}")
    @Operation(summary = "Get transaction by ID",
               description = "Retrieves a previously processed transaction along with its fraud evaluation result.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction found"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<TransactionResponseDTO> getTransaction(
            @PathVariable String transactionId) {

        log.debug("Fetching transaction: {}", transactionId);

        TransactionResponseDTO response = transactionService.getTransaction(transactionId);
        return ResponseEntity.ok(response);
    }
}
