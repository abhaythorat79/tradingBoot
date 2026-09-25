package com.abhi.algotrading.risk;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CapitalUtilizationCalculator {

    private final CapitalUtilizationConfig config;

    public CapitalUtilizationCalculator(
            CapitalUtilizationConfig config) {

        if (config == null) {
            throw new IllegalArgumentException(
                    "Capital utilization configuration cannot be null"
            );
        }

        this.config = config;
    }

    public BigDecimal calculateMaximumAllowedCapital(
            BigDecimal totalCapital) {

        validateCapital(totalCapital);

        return totalCapital
                .multiply(
                        config.maximumUtilizationPercent()
                )
                .divide(
                        new BigDecimal("100"),
                        10,
                        RoundingMode.HALF_UP
                );
    }

    public BigDecimal calculateUtilizedCapital(
            BigDecimal currentCommittedCapital,
            BigDecimal proposedPositionValue) {

        validateNonNegative(
                currentCommittedCapital,
                "Current committed capital"
        );

        validateNonNegative(
                proposedPositionValue,
                "Proposed position value"
        );

        return currentCommittedCapital.add(
                proposedPositionValue
        );
    }

    public boolean canOpenPosition(
            BigDecimal totalCapital,
            BigDecimal currentCommittedCapital,
            BigDecimal proposedPositionValue) {

        validateCapital(totalCapital);

        BigDecimal maximumAllowedCapital =
                calculateMaximumAllowedCapital(
                        totalCapital
                );

        BigDecimal utilizedCapital =
                calculateUtilizedCapital(
                        currentCommittedCapital,
                        proposedPositionValue
                );

        return utilizedCapital.compareTo(
                maximumAllowedCapital
        ) <= 0;
    }

    public BigDecimal calculateUtilizationPercent(
            BigDecimal totalCapital,
            BigDecimal utilizedCapital) {

        validateCapital(totalCapital);

        validateNonNegative(
                utilizedCapital,
                "Utilized capital"
        );

        return utilizedCapital
                .multiply(new BigDecimal("100"))
                .divide(
                        totalCapital,
                        10,
                        RoundingMode.HALF_UP
                );
    }

    private void validateCapital(
            BigDecimal totalCapital) {

        if (totalCapital == null ||
                totalCapital.signum() <= 0) {

            throw new IllegalArgumentException(
                    "Total capital must be positive"
            );
        }
    }

    private void validateNonNegative(
            BigDecimal value,
            String fieldName) {

        if (value == null ||
                value.signum() < 0) {

            throw new IllegalArgumentException(
                    fieldName +
                            " cannot be null or negative"
            );
        }
    }

    public CapitalUtilizationConfig getConfig() {
        return config;
    }
}