package com.abhi.algotrading.risk;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PositionSizer {

    public long calculateQuantity(
            BigDecimal capital,
            BigDecimal riskPercent,
            BigDecimal entryPrice,
            BigDecimal stopLossPrice) {

        if (capital == null ||
                capital.signum() <= 0) {

            throw new IllegalArgumentException(
                    "Capital must be positive"
            );
        }

        if (riskPercent == null ||
                riskPercent.signum() <= 0) {

            throw new IllegalArgumentException(
                    "Risk percentage must be positive"
            );
        }

        if (entryPrice == null ||
                entryPrice.signum() <= 0) {

            throw new IllegalArgumentException(
                    "Entry price must be positive"
            );
        }

        if (stopLossPrice == null ||
                stopLossPrice.signum() <= 0) {

            throw new IllegalArgumentException(
                    "Stop loss price must be positive"
            );
        }

        BigDecimal riskAmount =
                capital
                        .multiply(riskPercent)
                        .divide(
                                new BigDecimal("100"),
                                10,
                                RoundingMode.HALF_UP
                        );

        BigDecimal riskPerUnit =
                entryPrice.subtract(
                        stopLossPrice
                ).abs();

        if (riskPerUnit.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Entry and stop loss cannot be the same"
            );
        }

        return riskAmount
                .divide(
                        riskPerUnit,
                        0,
                        RoundingMode.DOWN
                )
                .longValue();
    }
}