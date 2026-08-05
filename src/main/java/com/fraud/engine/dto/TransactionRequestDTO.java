package com.fraud.engine.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * TransactionRequestDTO - Incoming transaction payload with validation
 * constraints.
 * 
 * Validation Rules: - amount: Must be positive, max 10 crore INR for Indian
 * context - currency: ISO 4217 format (INR, USD) - cardNumber: 16-19 digits,
 * Luhn validation recommended in production - idempotencyKey: Required for
 * exactly-once processing - customerId, merchantId: Required business
 * identifiers - ipAddress: Valid IPv4/IPv6 format - geoLocation: City, Country
 * format
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "cardNumber")
public class TransactionRequestDTO {

	@NotBlank(message = "Idempotency key is required")
	@Size(max = 128, message = "Idempotency key must not exceed 128 characters")
	private String idempotencyKey;

	@NotNull(message = "Amount is required")
	@DecimalMin(value = "0.01", message = "Amount must be greater than zero")
	@DecimalMax(value = "100000000.00", message = "Amount exceeds maximum allowed limit")
	@Digits(integer = 10, fraction = 2, message = "Amount format invalid")
	private BigDecimal amount;

	@NotBlank(message = "Currency is required")
	@Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be 3-letter ISO code (e.g., INR)")
	private String currency;

	@NotBlank(message = "Card number is required")
	@Pattern(regexp = "^\\d{16,19}$", message = "Card number must be 16-19 digits")
	private String cardNumber;

	@NotBlank(message = "Merchant ID is required")
	@Size(max = 64, message = "Merchant ID must not exceed 64 characters")
	private String merchantId;

	@NotBlank(message = "Merchant name is required")
	@Size(max = 128, message = "Merchant name must not exceed 128 characters")
	private String merchantName;

	@NotBlank(message = "Customer ID is required")
	@Size(max = 64, message = "Customer ID must not exceed 64 characters")
	private String customerId;

	@Pattern(regexp = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$", message = "Invalid IP address format")
	private String ipAddress;

	@Size(max = 128, message = "Geo location must not exceed 128 characters")
	private String geoLocation;

	@Size(max = 64, message = "Device ID must not exceed 64 characters")
	private String deviceId;

	private LocalDateTime timestamp;
}