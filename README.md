# Fraud Detection Rule Engine

## Overview
Enterprise-grade fraud detection system built with **Java 17**, **Spring Boot 3**, and the **Strategy Pattern**. Evaluates financial transactions against configurable rules and categorizes them as `SAFE`, `SUSPICIOUS`, or `FRAUD`.

## Architecture
```
Client (Postman/cURL)
    |
    v
FraudController (@RestController)
    |
    v
TransactionService (@Service)
    |-- IdempotencyService (exactly-once processing)
    |-- FraudEvaluationService (Strategy Pattern aggregation)
    |       |-- HighValueTransactionRule
    |       |-- FrequentTransactionRule
    |       |-- GeoLocationAnomalyRule
    |       |-- RepeatedPaymentFailureRule
    |
    v
Repositories (Spring Data JPA)
    |
    v
Database (PostgreSQL / MySQL)
```

## Design Patterns Used
1. **Strategy Pattern**: Fraud rules are interchangeable strategies
2. **DTO Pattern**: Separation between API and domain models
3. **Repository Pattern**: Data access abstraction
4. **Controller Advice**: Centralized exception handling

## Tech Stack
- Java 17
- Spring Boot 3.2.5
- Spring Data JPA + Hibernate
- PostgreSQL / MySQL
- Lombok
- Hibernate Validator
- OpenAPI 3 (Swagger)
- JUnit 5 + Mockito

## Database Setup

### PostgreSQL
```sql
CREATE DATABASE fraud_detection_db;
CREATE USER fraud_user WITH PASSWORD 'fraud_pass';
GRANT ALL PRIVILEGES ON DATABASE fraud_detection_db TO fraud_user;
```

### MySQL
```sql
CREATE DATABASE fraud_detection_db;
CREATE USER 'fraud_user'@'localhost' IDENTIFIED BY 'fraud_pass';
GRANT ALL PRIVILEGES ON fraud_detection_db.* TO 'fraud_user'@'localhost';
FLUSH PRIVILEGES;
```

### Run Schema & Data
```bash
# PostgreSQL
psql -U fraud_user -d fraud_detection_db -f sql/schema.sql
psql -U fraud_user -d fraud_detection_db -f sql/data.sql

# MySQL
mysql -u fraud_user -p fraud_detection_db < sql/schema.sql
mysql -u fraud_user -p fraud_detection_db < sql/data.sql
```

## Running the Application

### 1. Import into IDE
- **Eclipse/STS**: File -> Import -> Existing Maven Projects
- **IntelliJ**: File -> Open -> Select `pom.xml`

### 2. Configure Database
Edit `src/main/resources/application.properties`:
```properties
# For PostgreSQL (default)
spring.datasource.url=jdbc:postgresql://localhost:5432/fraud_detection_db

# For MySQL (uncomment and comment PostgreSQL)
# spring.datasource.url=jdbc:mysql://localhost:3306/fraud_detection_db?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true
```

### 3. Build & Run
```bash
mvn clean install
mvn spring-boot:run
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/fraud/evaluate` | Submit transaction for evaluation |
| GET | `/api/v1/fraud/transactions/{id}` | Retrieve transaction result |

## Swagger Documentation
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## Testing with Postman
Import `postman/Fraud-Detection-API-Collection.json` into Postman.

## Fraud Rules (Configurable)
1. **High Value Transaction**: Amount > INR 50,000 (Risk: 40)
2. **Frequent Transactions**: > 5 transactions in 10 minutes (Risk: 50)
3. **Geo Location Anomaly**: Transactions from high-risk/unusual locations (Risk: 35)
4. **Repeated Payment Failure**: 3+ failures in 30 minutes (Risk: 60)

## Risk Score Mapping
| Risk Score | Fraud Category | Action |
|------------|----------------|--------|
| 0 - 30 | SAFE | Approve transaction |
| 31 - 70 | SUSPICIOUS | Manual review required |
| 71 - 100 | FRAUD | Block transaction |

## Adding a New Fraud Rule
1. Create a class implementing `FraudRuleStrategy`
2. Add a record to the `fraud_rules` table
3. Restart the application

## Performance Optimizations
- Database indexes on `customer_id + timestamp`, `idempotency_key`
- Optimistic locking with `@Version`
- Read-only transactions for queries
- Count queries instead of loading full entities for velocity checks
- HikariCP connection pooling (max 20 connections)

## Interview Notes
- **Why Strategy Pattern?** Extensibility without modifying existing code (Open/Closed Principle)
- **How Idempotency Works?** UNIQUE constraint on `idempotency_key` + check-before-insert
- **Concurrency Safety?** `@Transactional` + Optimistic Locking (`@Version`)
- **N+1 Prevention?** Proper fetch strategies and indexed queries
- **PCI DSS Compliance?** Card numbers masked before storage (first 6 + last 4 only)

## Project Structure
```
fraud-detection-engine/
|-- pom.xml
|-- README.md
|-- sql/
|   |-- schema.sql
|   |-- data.sql
|-- postman/
|   |-- Fraud-Detection-API-Collection.json
|-- screenshots/
|-- src/
    |-- main/
    |   |-- java/com/fraud/engine/
    |   |   |-- FraudDetectionEngineApplication.java
    |   |   |-- config/
    |   |   |   |-- SwaggerConfig.java
    |   |   |-- controller/
    |   |   |   |-- FraudController.java
    |   |   |-- dto/
    |   |   |   |-- TransactionRequestDTO.java
    |   |   |   |-- TransactionResponseDTO.java
    |   |   |   |-- FraudResultDTO.java
    |   |   |   |-- ErrorResponseDTO.java
    |   |   |   |-- FraudRuleEvaluation.java
    |   |   |-- entity/
    |   |   |   |-- Transaction.java
    |   |   |   |-- FraudRule.java
    |   |   |   |-- FraudResult.java
    |   |   |-- enums/
    |   |   |   |-- TransactionStatus.java
    |   |   |   |-- FraudCategory.java
    |   |   |   |-- ReasonCode.java
    |   |   |-- exception/
    |   |   |   |-- FraudEngineException.java
    |   |   |   |-- DuplicateTransactionException.java
    |   |   |   |-- TransactionNotFoundException.java
    |   |   |   |-- RuleConfigurationException.java
    |   |   |   |-- GlobalExceptionHandler.java
    |   |   |-- repository/
    |   |   |   |-- TransactionRepository.java
    |   |   |   |-- FraudRuleRepository.java
    |   |   |   |-- FraudResultRepository.java
    |   |   |-- service/
    |   |   |   |-- TransactionService.java
    |   |   |   |-- FraudEvaluationService.java
    |   |   |   |-- IdempotencyService.java
    |   |   |-- strategy/
    |   |       |-- FraudRuleStrategy.java
    |   |       |-- HighValueTransactionRule.java
    |   |       |-- FrequentTransactionRule.java
    |   |       |-- GeoLocationAnomalyRule.java
    |   |       |-- RepeatedPaymentFailureRule.java
    |   |-- resources/
    |       |-- application.properties
    |-- test/
        |-- java/com/fraud/engine/
            |-- FraudDetectionEngineApplicationTests.java
            |-- controller/
            |   |-- FraudControllerTest.java
            |-- service/
            |   |-- TransactionServiceTest.java
            |-- strategy/
                |-- FraudRuleStrategyTest.java
```
