package com.fraud.engine.dto;

import com.fraud.engine.enums.ReasonCode;
import lombok.*;

/**
 * FraudRuleEvaluation - Internal DTO representing a single rule's evaluation result.
 * 
 * Used within the Strategy Pattern to communicate between individual rules
 * and the aggregation service.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudRuleEvaluation {

    private String ruleName;
    private boolean triggered;
    private ReasonCode reasonCode;
    private Integer riskScore;
    private String message;
}
