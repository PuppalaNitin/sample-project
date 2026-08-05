package com.fraud.engine.dto;

import com.fraud.engine.enums.FraudCategory;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * FraudResultDTO - Immutable fraud evaluation outcome.
 * 
 * Transferred as part of TransactionResponseDTO to provide
 * complete transparency in fraud decision-making.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudResultDTO {

    private FraudCategory category;
    private Integer riskScore;
    private List<String> reasonCodes;
    private List<String> rulesTriggered;
    private LocalDateTime evaluatedAt;
}
