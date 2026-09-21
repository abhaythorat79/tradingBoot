package com.abhi.algotrading.strategy;

import com.abhi.algotrading.marketdata.Candle;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BreakoutDetectorTest {

    private final BreakoutDetector detector =
            new BreakoutDetector();

    private final OpeningRange openingRange =
            new OpeningRange(
                    LocalDateTime.parse(
                            "2026-09-17T09:15"
                    ),
                    LocalDateTime.parse(
                            "2026-09-17T09:30"
                    ),
                    new BigDecimal("110"),
                    new BigDecimal("99")
            );

    @Test
    void shouldDetectUpwardBreakout() {

        Candle candle = candle(
                "09:35",
                "109",
                "112",
                "108",
                "111"
        );

        BreakoutDirection result =
                detector.detect(openingRange, candle);

        assertEquals(
                BreakoutDirection.UP,
                result
        );
    }

    @Test
    void shouldDetectDownwardBreakout() {

        Candle candle = candle(
                "09:35",
                "100",
                "101",
                "97",
                "98"
        );

        BreakoutDirection result =
                detector.detect(openingRange, candle);

        assertEquals(
                BreakoutDirection.DOWN,
                result
        );
    }

    @Test
    void shouldReturnNoneWhenThereIsNoBreakout() {

        Candle candle = candle(
                "09:35",
                "103",
                "108",
                "101",
                "106"
        );

        BreakoutDirection result =
                detector.detect(openingRange, candle);

        assertEquals(
                BreakoutDirection.NONE,
                result
        );
    }

    @Test
    void shouldDetectBothWhenBothSidesBreak() {

        Candle candle = candle(
                "09:35",
                "105",
                "112",
                "97",
                "104"
        );

        BreakoutDirection result =
                detector.detect(openingRange, candle);

        assertEquals(
                BreakoutDirection.BOTH,
                result
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