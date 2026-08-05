package com.fraud.engine.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ErrorResponseDTO - Standardized error response structure.
 * 
 * Follows RFC 7807 Problem Details principles adapted for enterprise APIs.
 * Includes timestamp, HTTP status, error code, message, and detailed errors.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponseDTO {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private int status;
    private String error;
    private String code;
    private String message;
    private String path;
    private List<String> details;
}
