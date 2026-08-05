package com.fraud.engine.repository;

import com.fraud.engine.entity.FraudResult;
import com.fraud.engine.enums.FraudCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * FraudResultRepository - Data access layer for fraud evaluation results.
 * 
 * Supports analytics queries for reporting and monitoring dashboards.
 */
@Repository
public interface FraudResultRepository extends JpaRepository<FraudResult, Long> {

    Optional<FraudResult> findByTransactionId(Long transactionId);

    @Query("SELECT fr FROM FraudResult fr JOIN FETCH fr.transaction t WHERE t.transactionId = :transactionId")
    Optional<FraudResult> findByTransactionTransactionId(@Param("transactionId") String transactionId);

    List<FraudResult> findByFraudCategory(FraudCategory fraudCategory);

    /**
     * Analytics query: Count fraud results by category in a time range.
     * Used for dashboard and reporting.
     */
    @Query("SELECT fr.fraudCategory, COUNT(fr) FROM FraudResult fr " +
           "WHERE fr.evaluatedAt BETWEEN :start AND :end GROUP BY fr.fraudCategory")
    List<Object[]> countByCategoryInTimeRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
