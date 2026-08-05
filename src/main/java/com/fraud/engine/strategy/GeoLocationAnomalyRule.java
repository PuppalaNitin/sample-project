package com.fraud.engine.strategy;

import com.fraud.engine.dto.FraudRuleEvaluation;
import com.fraud.engine.entity.FraudRule;
import com.fraud.engine.entity.Transaction;
import com.fraud.engine.enums.ReasonCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * GeoLocationAnomalyRule - Detects suspicious geographic transaction patterns.
 *
 * Business Logic: - In a production system, this would integrate with a
 * customer profile service to compare current location against historical
 * patterns. - Simplified implementation: Flags transactions from high-risk or
 * unusual locations based on a deny-list approach.
 *
 * Configuration Example: rule_name: GEO_LOCATION_ANOMALY rule_type: GEO_ANOMALY
 * risk_score: 35 priority: 30
 *
 * Indian Context: Flags transactions originating outside India or from known
 * high-fraud cities if customer profile indicates otherwise.
 */
@Slf4j
@Component
public class GeoLocationAnomalyRule implements FraudRuleStrategy {

	public static final String RULE_TYPE = "GEO_ANOMALY";

	// Simplified high-risk locations for demo purposes
	private static final Set<String> HIGH_RISK_COUNTRIES = Set.of("NIGERIA", "BANGLADESH", "PAKISTAN");

	// FIXED: Removed duplicate "NOIDA" entry
	private static final Set<String> INDIAN_CITIES = Set.of("MUMBAI", "DELHI", "BANGALORE", "HYDERABAD", "CHENNAI",
			"KOLKATA", "PUNE", "AHMEDABAD", "JAIPUR", "LUCKNOW", "KANPUR", "NAGPUR", "INDORE", "THANE", "BHOPAL",
			"VISAKHAPATNAM", "PATNA", "VADODARA", "GHAZIABAD", "LUDHIANA", "AGRA", "NASHIK", "FARIDABAD", "MEERUT",
			"RAJKOT", "KALYAN", "VASAI", "VARANASI", "SRINAGAR", "AURANGABAD", "DHANBAD", "AMRITSAR", "NAVI MUMBAI",
			"ALLAHABAD", "RANCHI", "HOWRAH", "COIMBATORE", "JABALPUR", "GWALIOR", "VIJAYAWADA", "JODHPUR", "MADURAI",
			"RAIPUR", "KOTA", "GUWAHATI", "CHANDIGARH", "SOLAPUR", "HUBLI", "TIRUCHIRAPPALLI", "TIRUPPUR", "GURGAON",
			"NOIDA");

	@Override
	public String getRuleType() {
		return RULE_TYPE;
	}

	@Override
	public FraudRuleEvaluation evaluate(Transaction transaction, List<Transaction> recentTransactions,
			FraudRule ruleConfig) {

		log.debug("Evaluating GeoLocationAnomalyRule for transaction: {}", transaction.getTransactionId());

		String geoLocation = transaction.getGeoLocation();
		if (geoLocation == null || geoLocation.isBlank()) {
			log.debug("No geo-location data available for transaction {}", transaction.getTransactionId());
			return FraudRuleEvaluation.builder().ruleName(ruleConfig.getRuleName()).triggered(false).riskScore(0)
					.message("No geo-location data available").build();
		}

		String normalizedLocation = geoLocation.toUpperCase().trim();

		// Check for high-risk countries
		for (String country : HIGH_RISK_COUNTRIES) {
			if (normalizedLocation.contains(country)) {
				log.warn("GEO_ANOMALY triggered: High-risk country detected for transaction {} - {}",
						transaction.getTransactionId(), geoLocation);
				return FraudRuleEvaluation.builder().ruleName(ruleConfig.getRuleName()).triggered(true)
						.reasonCode(ReasonCode.GEO_LOCATION_ANOMALY).riskScore(ruleConfig.getRiskScore())
						.message("Transaction from high-risk location: " + geoLocation).build();
			}
		}

		// Check if location is outside India (simplified: not in known Indian cities)
		boolean isKnownIndianCity = INDIAN_CITIES.stream().anyMatch(normalizedLocation::contains);

		if (!isKnownIndianCity && !normalizedLocation.contains("INDIA")) {
			log.info("GEO_ANOMALY triggered: Unusual location for transaction {} - {}", transaction.getTransactionId(),
					geoLocation);
			return FraudRuleEvaluation.builder().ruleName(ruleConfig.getRuleName()).triggered(true)
					.reasonCode(ReasonCode.GEO_LOCATION_ANOMALY).riskScore(ruleConfig.getRiskScore())
					.message("Transaction location outside known customer region: " + geoLocation).build();
		}

		return FraudRuleEvaluation.builder().ruleName(ruleConfig.getRuleName()).triggered(false).riskScore(0)
				.message("Geo-location within acceptable parameters").build();
	}
}