package com.abhi.algotrading.risk;

import java.math.BigDecimal;

public record CapitalUtilizationConfig(
        BigDecimal maximumUtilizationPercent
) {

    public CapitalUtilizationConfig {

        if (maximumUtilizationPercent == null) {
            throw new IllegalArgumentException(
                    "Maximum capital utilization percentage cannot be null"
            );
        }

        if (maximumUtilizationPercent.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Maximum capital utilization percentage must be positive"
            );
        }

        if (maximumUtilizationPercent.compareTo(
                new BigDecimal("100")) > 0) {

            throw new IllegalArgumentException(
                    "Maximum capital utilization percentage cannot exceed 100"
            );
        }
    }
}