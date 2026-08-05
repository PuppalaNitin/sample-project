-- ============================================
-- Fraud Detection Rule Engine - MySQL Sample Data
-- ============================================

INSERT INTO fraud_rules (rule_name, rule_type, priority, enabled, threshold_amount, threshold_count, time_window_minutes, risk_score, description) VALUES
('HIGH_VALUE_TRANSACTION', 'HIGH_VALUE', 10, true, 50000.00, NULL, NULL, 40, 'Flags transactions above INR 50,000'),
('FREQUENT_TRANSACTIONS', 'FREQUENT', 20, true, NULL, 5, 10, 50, 'Flags more than 5 transactions in 10 minutes'),
('GEO_LOCATION_ANOMALY', 'GEO_ANOMALY', 30, true, NULL, NULL, NULL, 35, 'Flags transactions from unusual or high-risk locations'),
('REPEATED_PAYMENT_FAILURE', 'REPEATED_FAILURE', 15, true, NULL, 3, 30, 60, 'Flags 3+ failures in 30 minutes indicating card testing');

INSERT INTO transactions (transaction_id, idempotency_key, amount, currency, card_number, merchant_id, merchant_name, customer_id, ip_address, geo_location, device_id, transaction_timestamp, status, version) VALUES
('TXN-001-UUID', 'idem-001-abc', 1250.00, 'INR', '411111XXXXXX1111', 'FLIPKART001', 'Flipkart', 'CUST100001', '103.21.58.90', 'Mumbai, India', 'DEV-ANDROID-001', DATE_SUB(NOW(), INTERVAL 5 MINUTE), 'COMPLETED', 1),
('TXN-002-UUID', 'idem-002-def', 750.50, 'INR', '411111XXXXXX1111', 'SWIGGY001', 'Swiggy', 'CUST100001', '103.21.58.90', 'Mumbai, India', 'DEV-ANDROID-001', DATE_SUB(NOW(), INTERVAL 3 MINUTE), 'COMPLETED', 1),
('TXN-003-UUID', 'idem-003-ghi', 890.00, 'INR', '411111XXXXXX1111', 'ZOMATO001', 'Zomato', 'CUST100001', '103.21.58.90', 'Mumbai, India', 'DEV-ANDROID-001', DATE_SUB(NOW(), INTERVAL 2 MINUTE), 'COMPLETED', 1),
('TXN-004-UUID', 'idem-004-jkl', 450.00, 'INR', '411111XXXXXX1111', 'AMAZON001', 'Amazon India', 'CUST100001', '103.21.58.90', 'Mumbai, India', 'DEV-ANDROID-001', DATE_SUB(NOW(), INTERVAL 1 MINUTE), 'COMPLETED', 1),
('TXN-005-UUID', 'idem-005-mno', 320.00, 'INR', '411111XXXXXX1111', 'PAYTM001', 'Paytm', 'CUST100001', '103.21.58.90', 'Mumbai, India', 'DEV-ANDROID-001', DATE_SUB(NOW(), INTERVAL 30 SECOND), 'COMPLETED', 1),
('TXN-006-UUID', 'idem-006-pqr', 125000.00, 'INR', '550000XXXXXX8888', 'FLIPKART001', 'Flipkart', 'CUST200002', '182.76.45.12', 'Delhi, India', 'DEV-IOS-002', DATE_SUB(NOW(), INTERVAL 10 MINUTE), 'COMPLETED', 1),
('TXN-007-UUID', 'idem-007-stu', 2500.00, 'INR', '550000XXXXXX8888', 'PHONEPE001', 'PhonePe', 'CUST200002', '182.76.45.12', 'Delhi, India', 'DEV-IOS-002', DATE_SUB(NOW(), INTERVAL 8 MINUTE), 'COMPLETED', 1),
('TXN-008-UUID', 'idem-008-vwx', 999.00, 'INR', '377777XXXXXX9999', 'SWIGGY001', 'Swiggy', 'CUST300003', '45.123.67.89', 'Bangalore, India', 'DEV-WEB-003', DATE_SUB(NOW(), INTERVAL 15 MINUTE), 'COMPLETED', 1),
('TXN-009-UUID', 'idem-009-yza', 15000.00, 'INR', '377777XXXXXX9999', 'AMAZON001', 'Amazon India', 'CUST300003', '45.123.67.89', 'Bangalore, India', 'DEV-WEB-003', DATE_SUB(NOW(), INTERVAL 12 MINUTE), 'COMPLETED', 1),
('TXN-010-UUID', 'idem-010-bcd', 85000.00, 'INR', '411111XXXXXX2222', 'FLIPKART001', 'Flipkart', 'CUST400004', '198.51.100.45', 'Lagos, Nigeria', 'DEV-UNKNOWN-999', DATE_SUB(NOW(), INTERVAL 20 MINUTE), 'COMPLETED', 1);

INSERT INTO fraud_results (transaction_id, fraud_category, reason_codes, risk_score, rules_triggered, evaluated_at) VALUES
(1, 'SAFE', NULL, 0, NULL, DATE_SUB(NOW(), INTERVAL 5 MINUTE)),
(2, 'SAFE', NULL, 0, NULL, DATE_SUB(NOW(), INTERVAL 3 MINUTE)),
(3, 'SAFE', NULL, 0, NULL, DATE_SUB(NOW(), INTERVAL 2 MINUTE)),
(4, 'SAFE', NULL, 0, NULL, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
(5, 'SUSPICIOUS', 'F002', 50, 'FREQUENT_TRANSACTIONS', DATE_SUB(NOW(), INTERVAL 30 SECOND)),
(6, 'SUSPICIOUS', 'F001', 40, 'HIGH_VALUE_TRANSACTION', DATE_SUB(NOW(), INTERVAL 10 MINUTE)),
(7, 'SAFE', NULL, 0, NULL, DATE_SUB(NOW(), INTERVAL 8 MINUTE)),
(8, 'SAFE', NULL, 0, NULL, DATE_SUB(NOW(), INTERVAL 15 MINUTE)),
(9, 'SAFE', NULL, 0, NULL, DATE_SUB(NOW(), INTERVAL 12 MINUTE)),
(10, 'FRAUD', 'F001,F003', 75, 'HIGH_VALUE_TRANSACTION,GEO_LOCATION_ANOMALY', DATE_SUB(NOW(), INTERVAL 20 MINUTE));