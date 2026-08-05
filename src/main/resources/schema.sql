-- ============================================
-- Fraud Detection Rule Engine - MySQL Schema
-- Idempotent: Safe to run multiple times
-- ============================================

-- Drop tables in reverse dependency order (child first, parent last)
DROP TABLE IF EXISTS fraud_results;
DROP TABLE IF EXISTS fraud_rules;
DROP TABLE IF EXISTS transactions;

-- ============================================
-- Table: transactions
-- ============================================
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(64) NOT NULL UNIQUE,
    idempotency_key VARCHAR(128) NOT NULL UNIQUE,
    amount DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    card_number VARCHAR(19) NOT NULL,
    merchant_id VARCHAR(64) NOT NULL,
    merchant_name VARCHAR(128) NOT NULL,
    customer_id VARCHAR(64) NOT NULL,
    ip_address VARCHAR(45),
    geo_location VARCHAR(128),
    device_id VARCHAR(64),
    transaction_timestamp TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE INDEX idx_transaction_customer_time ON transactions(customer_id, transaction_timestamp);
CREATE INDEX idx_transaction_status ON transactions(status);
CREATE INDEX idx_transaction_merchant ON transactions(merchant_id);

-- ============================================
-- Table: fraud_rules
-- ============================================
CREATE TABLE IF NOT EXISTS fraud_rules (
    id INT AUTO_INCREMENT PRIMARY KEY,
    rule_name VARCHAR(64) NOT NULL UNIQUE,
    rule_type VARCHAR(64) NOT NULL,
    priority INT NOT NULL DEFAULT 100,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    threshold_amount DECIMAL(15, 2),
    threshold_count INT,
    time_window_minutes INT,
    risk_score INT NOT NULL DEFAULT 25,
    description VARCHAR(512),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE INDEX idx_fraud_rule_priority ON fraud_rules(priority);
CREATE INDEX idx_fraud_rule_enabled ON fraud_rules(enabled);

-- ============================================
-- Table: fraud_results
-- ============================================
CREATE TABLE IF NOT EXISTS fraud_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT NOT NULL UNIQUE,
    fraud_category VARCHAR(20) NOT NULL,
    reason_codes VARCHAR(256),
    risk_score INT NOT NULL DEFAULT 0,
    rules_triggered VARCHAR(512),
    evaluated_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fraud_result_transaction 
        FOREIGN KEY (transaction_id) REFERENCES transactions(id)
) ENGINE=InnoDB;

CREATE INDEX idx_fraud_result_category ON fraud_results(fraud_category);
CREATE INDEX idx_fraud_result_evaluated ON fraud_results(evaluated_at);