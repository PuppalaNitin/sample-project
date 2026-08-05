package com.fraud.engine.exception;

import com.fraud.engine.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * GlobalExceptionHandler - Centralized exception handling for all REST controllers.
 * 
 * Design Pattern: This is an implementation of the Controller Advice pattern in Spring,
 * which separates error handling logic from business logic controllers.
 * 
 * Benefits:
 * 1. Consistent error response format across all APIs
 * 2. Single point of maintenance for HTTP status mapping
 * 3. Proper logging of exceptions with context
 * 4. No try-catch blocks cluttering controller code
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateTransactionException.class)
    public ResponseEntity<ErrorResponseDTO> handleDuplicateTransaction(
            DuplicateTransactionException ex, HttpServletRequest request) {

        log.warn("Duplicate transaction detected: {}", ex.getMessage());

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .code(ex.getErrorCode())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleTransactionNotFound(
            TransactionNotFoundException ex, HttpServletRequest request) {

        log.warn("Transaction not found: {}", ex.getMessage());

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .code(ex.getErrorCode())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(RuleConfigurationException.class)
    public ResponseEntity<ErrorResponseDTO> handleRuleConfiguration(
            RuleConfigurationException ex, HttpServletRequest request) {

        log.error("Rule configuration error: {}", ex.getMessage(), ex);

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .code(ex.getErrorCode())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<String> details = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            details.add(fieldName + ": " + errorMessage);
        });

        log.warn("Validation failed for request to {}: {}", request.getRequestURI(), details);

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .code("ERR_VALIDATION_FAILED")
                .message("Request validation failed")
                .path(request.getRequestURI())
                .details(details)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        List<String> details = new ArrayList<>();
        ex.getConstraintViolations().forEach(violation -> 
            details.add(violation.getPropertyPath() + ": " + violation.getMessage())
        );

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .code("ERR_CONSTRAINT_VIOLATION")
                .message("Constraint violation detected")
                .path(request.getRequestURI())
                .details(details)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected error processing request to {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .code("ERR_INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred. Please contact support.")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
