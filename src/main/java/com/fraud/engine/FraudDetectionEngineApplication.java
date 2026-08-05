package com.fraud.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Fraud Detection Rule Engine - Entry Point
 * 
 * Enterprise-grade fraud detection using Strategy Pattern for extensible rule evaluation.
 * Supports idempotent processing, concurrency-safe transaction handling, and real-time
 * fraud categorization (SAFE, SUSPICIOUS, FRAUD).
 * 
 * @author Fraud Engine Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableAsync
public class FraudDetectionEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(FraudDetectionEngineApplication.class, args);
    }
}
