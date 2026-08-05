package com.fraud.engine.dto;

import com.fraud.engine.enums.FraudCategory;
import com.fraud.engine.enums.TransactionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * TransactionResponseDTO - Response payload after fraud evaluation.
 * 
 * Contains the original transaction data enriched with fraud detection results.
 * The fraudResult field is null if evaluation is pending or failed.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponseDTO {

    private String transactionId;
    private String idempotencyKey;
    private BigDecimal amount;
    private String currency;
    private String cardNumberMasked;
    private String merchantName;
    private String customerId;
    private String geoLocation;
    private TransactionStatus status;
    private LocalDateTime timestamp;
    private LocalDateTime processedAt;
    private FraudResultDTO fraudResult;
}
