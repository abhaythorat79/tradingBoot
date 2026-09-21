package com.abhi.algotrading.confirmation;

import com.abhi.algotrading.marketdata.Candle;
import com.abhi.algotrading.strategy.BreakoutDirection;
import com.abhi.algotrading.strategy.OpeningRange;

public class CandleConfirmation {

    public ConfirmationResult confirm(
            OpeningRange openingRange,
            Candle candle,
            BreakoutDirection direction) {

        if (openingRange == null) {
            throw new IllegalArgumentException(
                    "Opening range cannot be null"
            );
        }

        if (candle == null) {
            throw new IllegalArgumentException(
                    "Candle cannot be null"
            );
        }

        if (direction == null) {
            throw new IllegalArgumentException(
                    "Breakout direction cannot be null"
            );
        }

        if (direction == BreakoutDirection.UP) {

            boolean confirmed =
                    candle.close().compareTo(
                            openingRange.high()
                    ) > 0;

            return confirmed
                    ? ConfirmationResult.CONFIRMED
                    : ConfirmationResult.NOT_CONFIRMED;
        }

        if (direction == BreakoutDirection.DOWN) {

            boolean confirmed =
                    candle.close().compareTo(
                            openingRange.low()
                    ) < 0;

            return confirmed
                    ? ConfirmationResult.CONFIRMED
                    : ConfirmationResult.NOT_CONFIRMED;
        }

        /*
         * NONE and BOTH are not valid entry directions.
         */
        return ConfirmationResult.NOT_CONFIRMED;
    }
}