package com.abhi.algotrading.risk;

import com.abhi.algotrading.portfolio.Position;
import com.abhi.algotrading.portfolio.PositionSide;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TrailingStopManager {

    private final BigDecimal trailingStopPercent;

    public TrailingStopManager(
            BigDecimal trailingStopPercent) {

        if (trailingStopPercent == null ||
                trailingStopPercent.signum() <= 0) {

            throw new IllegalArgumentException(
                    "Trailing stop percentage must be positive"
            );
        }

        this.trailingStopPercent =
                trailingStopPercent;
    }

    public void update(
            Position position,
            BigDecimal marketPrice) {

        if (position == null) {
            throw new IllegalArgumentException(
                    "Position cannot be null"
            );
        }

        if (marketPrice == null ||
                marketPrice.signum() <= 0) {

            throw new IllegalArgumentException(
                    "Market price must be positive"
            );
        }

        /*
         * Trailing starts ONLY after the initial
         * target has been reached.
         *
         * Before that, the original stop loss
         * remains unchanged.
         */
        if (position.hasFixedTarget()) {
            return;
        }

        BigDecimal newStopLoss;

        if (position.getSide() ==
                PositionSide.LONG) {

            newStopLoss =
                    calculateLongStop(marketPrice);

        } else {

            newStopLoss =
                    calculateShortStop(marketPrice);
        }

        /*
         * Position itself guarantees that the stop
         * can only move in the profitable direction.
         */
        position.updateTrailingStop(
                newStopLoss
        );
    }

    private BigDecimal calculateLongStop(
            BigDecimal marketPrice) {

        BigDecimal trailingAmount =
                marketPrice
                        .multiply(trailingStopPercent)
                        .divide(
                                new BigDecimal("100"),
                                10,
                                RoundingMode.HALF_UP
                        );

        return marketPrice.subtract(
                trailingAmount
        );
    }

    private BigDecimal calculateShortStop(
            BigDecimal marketPrice) {

        BigDecimal trailingAmount =
                marketPrice
                        .multiply(trailingStopPercent)
                        .divide(
                                new BigDecimal("100"),
                                10,
                                RoundingMode.HALF_UP
                        );

        return marketPrice.add(
                trailingAmount
        );
    }

    public BigDecimal getTrailingStopPercent() {
        return trailingStopPercent;
    }
}