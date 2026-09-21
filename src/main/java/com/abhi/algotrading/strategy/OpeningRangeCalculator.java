package com.abhi.algotrading.strategy;

import com.abhi.algotrading.common.TradingSession;
import com.abhi.algotrading.marketdata.Candle;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class OpeningRangeCalculator {

    public OpeningRange calculate(List<Candle> candles) {

        if (candles == null || candles.isEmpty()) {
            throw new IllegalArgumentException(
                    "Candle data cannot be empty"
            );
        }

        /*
         * We calculate the opening range only for the
         * first trading date present in the input.
         */
        LocalDate tradingDate =
                candles.getFirst().timestamp().toLocalDate();

        /*
         * Select only candles belonging to:
         *
         * 09:15 <= candle time < 09:30
         *
         * Therefore:
         *
         * 09:15 -> included
         * 09:20 -> included
         * 09:25 -> included
         * 09:30 -> excluded
         * 09:35 -> excluded
         */
        List<Candle> openingCandles = candles.stream()
                .filter(candle ->
                        isOpeningRangeCandle(candle, tradingDate)
                )
                .toList();

        if (openingCandles.isEmpty()) {
            throw new IllegalArgumentException(
                    "No valid opening range candles found"
            );
        }

        BigDecimal high = openingCandles.stream()
                .map(Candle::high)
                .max(BigDecimal::compareTo)
                .orElseThrow();

        BigDecimal low = openingCandles.stream()
                .map(Candle::low)
                .min(BigDecimal::compareTo)
                .orElseThrow();

        /*
         * IMPORTANT:
         *
         * The OpeningRange represents the complete
         * 09:15 -> 09:30 window.
         *
         * It should NOT use the timestamp of the last
         * candle (09:25).
         */
        LocalDateTime startTime =
                LocalDateTime.of(
                        tradingDate,
                        TradingSession.MARKET_OPEN
                );

        LocalDateTime endTime =
                LocalDateTime.of(
                        tradingDate,
                        TradingSession.OPENING_RANGE_END
                );

        return new OpeningRange(
                startTime,
                endTime,
                high,
                low
        );
    }

    private boolean isOpeningRangeCandle(
            Candle candle,
            LocalDate tradingDate) {

        LocalDateTime timestamp =
                candle.timestamp();

        /*
         * Ignore candles from other dates.
         */
        if (!timestamp.toLocalDate().equals(tradingDate)) {
            return false;
        }

        LocalTime time =
                timestamp.toLocalTime();

        /*
         * Opening range:
         *
         * 09:15 inclusive
         * 09:30 exclusive
         */
        return !time.isBefore(
                TradingSession.MARKET_OPEN
        )
                && time.isBefore(
                TradingSession.OPENING_RANGE_END
        );
    }
}