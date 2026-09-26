package com.abhi.algotrading.risk;

import java.math.BigDecimal;

public class PositionCapitalCalculator {

    public BigDecimal calculatePositionValue(
            BigDecimal entryPrice,
            long quantity) {

        if (entryPrice == null ||
                entryPrice.signum() <= 0) {

            throw new IllegalArgumentException(
                    "Entry price must be positive"
            );
        }

        if (quantity <= 0) {

            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        return entryPrice.multiply(
                BigDecimal.valueOf(quantity)
        );
    }
}