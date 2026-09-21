package com.abhi.algotrading.strategy;

import com.abhi.algotrading.marketdata.Candle;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OpeningRangeCalculatorTest {

    private final OpeningRangeCalculator calculator =
            new OpeningRangeCalculator();

    @Test
    void shouldCalculateOpeningRange() {

        List<Candle> candles = List.of(
                candle("09:15", "100", "105", "99", "103"),
                candle("09:20", "103", "108", "102", "107"),
                candle("09:25", "107", "110", "106", "109")
        );

        OpeningRange result =
                calculator.calculate(candles);

        assertEquals(
                new BigDecimal("110"),
                result.high()
        );

        assertEquals(
                new BigDecimal("99"),
                result.low()
        );

        assertEquals(
                new BigDecimal("11"),
                result.range()
        );
    }

    @Test
    void shouldIgnoreCandlesAfterOpeningRange() {

        List<Candle> candles = List.of(
                candle("09:15", "100", "105", "99", "103"),
                candle("09:20", "103", "108", "102", "107"),
                candle("09:25", "107", "110", "106", "109"),

                // Must NOT affect opening range
                candle("09:30", "109", "150", "80", "120"),

                // Must NOT affect opening range
                candle("09:35", "120", "200", "50", "180")
        );

        OpeningRange result =
                calculator.calculate(candles);

        assertEquals(
                new BigDecimal("110"),
                result.high()
        );

        assertEquals(
                new BigDecimal("99"),
                result.low()
        );
    }

    @Test
    void shouldSetCorrectOpeningRangeTime() {

        List<Candle> candles = List.of(
                candle("09:15", "100", "105", "99", "103"),
                candle("09:20", "103", "108", "102", "107"),
                candle("09:25", "107", "110", "106", "109")
        );

        OpeningRange result =
                calculator.calculate(candles);

        assertEquals(
                LocalDateTime.parse(
                        "2026-09-17T09:15"
                ),
                result.startTime()
        );

        assertEquals(
                LocalDateTime.parse(
                        "2026-09-17T09:30"
                ),
                result.endTime()
        );
    }

    private Candle candle(
            String time,
            String open,
            String high,
            String low,
            String close) {

        return new Candle(
                LocalDateTime.parse(
                        "2026-09-17T" + time
                ),
                "TEST",
                new BigDecimal(open),
                new BigDecimal(high),
                new BigDecimal(low),
                new BigDecimal(close),
                100_000
        );
    }
}