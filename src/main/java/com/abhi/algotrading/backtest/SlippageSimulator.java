package com.abhi.algotrading.backtest;

import com.abhi.algotrading.portfolio.PositionSide;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SlippageSimulator {

    private final BigDecimal slippagePercent;

    public SlippageSimulator(BigDecimal slippagePercent) {

        if (slippagePercent == null) {
            throw new IllegalArgumentException(
                    "Slippage percentage cannot be null"
            );
        }

        if (slippagePercent.signum() < 0) {
            throw new IllegalArgumentException(
                    "Slippage percentage cannot be negative"
            );
        }

        this.slippagePercent = slippagePercent;
    }

    public BigDecimal simulateEntry(
            PositionSide side,
            BigDecimal theoreticalPrice) {

        validateInputs(side, theoreticalPrice);

        /*
         * Zero slippage must return the original
         * price object/value unchanged.
         *
         * This preserves the original BigDecimal
         * scale used by the backtest when no
         * slippage is configured.
         */
        if (slippagePercent.signum() == 0) {
            return theoreticalPrice;
        }

        BigDecimal slippageAmount =
                theoreticalPrice
                        .multiply(slippagePercent)
                        .divide(
                                new BigDecimal("100"),
                                10,
                                RoundingMode.HALF_UP
                        );

        /*
         * Entry is always executed at a worse price:
         *
         * LONG  -> buy higher
         * SHORT -> sell lower
         */
        if (side == PositionSide.LONG) {

            return theoreticalPrice.add(
                    slippageAmount
            );

        } else {

            return theoreticalPrice.subtract(
                    slippageAmount
            );
        }
    }

    public BigDecimal simulateExit(
            PositionSide side,
            BigDecimal theoreticalPrice) {

        validateInputs(side, theoreticalPrice);

        /*
         * Zero slippage must return the original
         * price unchanged.
         */
        if (slippagePercent.signum() == 0) {
            return theoreticalPrice;
        }

        BigDecimal slippageAmount =
                theoreticalPrice
                        .multiply(slippagePercent)
                        .divide(
                                new BigDecimal("100"),
                                10,
                                RoundingMode.HALF_UP
                        );

        /*
         * Exit is always executed at a worse price:
         *
         * LONG  -> sell lower
         * SHORT -> buy higher
         */
        if (side == PositionSide.LONG) {

            return theoreticalPrice.subtract(
                    slippageAmount
            );

        } else {

            return theoreticalPrice.add(
                    slippageAmount
            );
        }
    }

    private void validateInputs(
            PositionSide side,
            BigDecimal price) {

        if (side == null) {
            throw new IllegalArgumentException(
                    "Position side cannot be null"
            );
        }

        if (price == null ||
                price.signum() <= 0) {

            throw new IllegalArgumentException(
                    "Price must be positive"
            );
        }
    }

    public BigDecimal getSlippagePercent() {
        return slippagePercent;
    }
}