package com.abhi.algotrading.risk;

import java.math.BigDecimal;

public record RiskConfig(

        BigDecimal capital,

        BigDecimal riskPerTradePercent,

        BigDecimal maxDailyLoss,

        int maxTradesPerDay,

        int maxConsecutiveLosses,

        BigDecimal stopLossPercent,

        BigDecimal targetPercent,

        BigDecimal trailingStopPercent

) {

    public RiskConfig {

        if (capital == null || capital.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Capital must be positive"
            );
        }

        if (riskPerTradePercent == null ||
                riskPerTradePercent.signum() <= 0) {

            throw new IllegalArgumentException(
                    "Risk per trade must be positive"
            );
        }

        if (maxDailyLoss == null ||
                maxDailyLoss.signum() <= 0) {

            throw new IllegalArgumentException(
                    "Maximum daily loss must be positive"
            );
        }

        if (maxTradesPerDay <= 0) {
            throw new IllegalArgumentException(
                    "Maximum trades per day must be positive"
            );
        }

        if (maxConsecutiveLosses <= 0) {
            throw new IllegalArgumentException(
                    "Maximum consecutive losses must be positive"
            );
        }

        if (stopLossPercent == null ||
                stopLossPercent.signum() <= 0) {

            throw new IllegalArgumentException(
                    "Stop loss percentage must be positive"
            );
        }

        if (targetPercent == null ||
                targetPercent.signum() <= 0) {

            throw new IllegalArgumentException(
                    "Target percentage must be positive"
            );
        }

        if (trailingStopPercent == null ||
                trailingStopPercent.signum() <= 0) {

            throw new IllegalArgumentException(
                    "Trailing stop percentage must be positive"
            );
        }
    }
}