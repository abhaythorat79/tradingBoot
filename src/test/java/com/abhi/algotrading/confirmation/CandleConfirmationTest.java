package com.abhi.algotrading.confirmation;

import com.abhi.algotrading.marketdata.Candle;
import com.abhi.algotrading.strategy.BreakoutDirection;
import com.abhi.algotrading.strategy.OpeningRange;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CandleConfirmationTest {

    private final CandleConfirmation confirmation =
            new CandleConfirmation();

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
    void shouldConfirmUpwardBreakoutWhenCandleClosesAboveRangeHigh() {

        Candle candle = candle(
                "09:35",
                "109",
                "112",
                "108",
                "111"
        );

        ConfirmationResult result =
                confirmation.confirm(
                        openingRange,
                        candle,
                        BreakoutDirection.UP
                );

        assertEquals(
                ConfirmationResult.CONFIRMED,
                result
        );
    }

    @Test
    void shouldNotConfirmUpwardBreakoutWhenCandleClosesInsideRange() {

        Candle candle = candle(
                "09:35",
                "109",
                "112",
                "108",
                "109"
        );

        ConfirmationResult result =
                confirmation.confirm(
                        openingRange,
                        candle,
                        BreakoutDirection.UP
                );

        assertEquals(
                ConfirmationResult.NOT_CONFIRMED,
                result
        );
    }

    @Test
    void shouldConfirmDownwardBreakoutWhenCandleClosesBelowRangeLow() {

        Candle candle = candle(
                "09:35",
                "100",
                "101",
                "97",
                "98"
        );

        ConfirmationResult result =
                confirmation.confirm(
                        openingRange,
                        candle,
                        BreakoutDirection.DOWN
                );

        assertEquals(
                ConfirmationResult.CONFIRMED,
                result
        );
    }

    @Test
    void shouldNotConfirmDownwardBreakoutWhenCandleClosesInsideRange() {

        Candle candle = candle(
                "09:35",
                "100",
                "101",
                "97",
                "100"
        );

        ConfirmationResult result =
                confirmation.confirm(
                        openingRange,
                        candle,
                        BreakoutDirection.DOWN
                );

        assertEquals(
                ConfirmationResult.NOT_CONFIRMED,
                result
        );
    }

    @Test
    void shouldNotConfirmNoneDirection() {

        Candle candle = candle(
                "09:35",
                "103",
                "108",
                "101",
                "106"
        );

        ConfirmationResult result =
                confirmation.confirm(
                        openingRange,
                        candle,
                        BreakoutDirection.NONE
                );

        assertEquals(
                ConfirmationResult.NOT_CONFIRMED,
                result
        );
    }

    @Test
    void shouldNotConfirmBothDirection() {

        Candle candle = candle(
                "09:35",
                "105",
                "112",
                "97",
                "104"
        );

        ConfirmationResult result =
                confirmation.confirm(
                        openingRange,
                        candle,
                        BreakoutDirection.BOTH
                );

        assertEquals(
                ConfirmationResult.NOT_CONFIRMED,
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