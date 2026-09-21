package com.abhi.algotrading.strategy;

import com.abhi.algotrading.marketdata.Candle;

import java.math.BigDecimal;

public class BreakoutDetector {

    public BreakoutDirection detect(
            OpeningRange openingRange,
            Candle candle) {

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

        BigDecimal rangeHigh = openingRange.high();
        BigDecimal rangeLow = openingRange.low();

        BigDecimal candleHigh = candle.high();
        BigDecimal candleLow = candle.low();

        boolean brokeUpperRange =
                candleHigh.compareTo(rangeHigh) > 0;

        boolean brokeLowerRange =
                candleLow.compareTo(rangeLow) < 0;

        if (brokeUpperRange && brokeLowerRange) {
            return BreakoutDirection.BOTH;
        }

        if (brokeUpperRange) {
            return BreakoutDirection.UP;
        }

        if (brokeLowerRange) {
            return BreakoutDirection.DOWN;
        }

        return BreakoutDirection.NONE;
    }
}