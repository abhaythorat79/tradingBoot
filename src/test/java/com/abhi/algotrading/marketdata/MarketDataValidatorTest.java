package com.abhi.algotrading.marketdata;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MarketDataValidatorTest {

    private final MarketDataValidator validator =
            new MarketDataValidator();

    @Test
    void shouldAcceptValidCandles() {

        List<Candle> candles =
                List.of(
                        candle(
                                "09:15:00",
                                "NIFTY",
                                "25000",
                                "25020",
                                "24990",
                                "25010",
                                100000
                        ),
                        candle(
                                "09:16:00",
                                "NIFTY",
                                "25010",
                                "25030",
                                "25000",
                                "25025",
                                120000
                        )
                );

        assertDoesNotThrow(
                () -> validator.validate(candles)
        );
    }

    @Test
    void shouldRejectEmptyData() {

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(List.of())
        );
    }

    @Test
    void shouldRejectNullData() {

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(null)
        );
    }

    @Test
    void shouldRejectHighLowerThanLow() {

        List<Candle> candles =
                List.of(
                        candle(
                                "09:15:00",
                                "NIFTY",
                                "24850",
                                "24800",
                                "24900",
                                "24850",
                                100000
                        )
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(candles)
        );
    }

    @Test
    void shouldRejectOpenOutsideHighLow() {

        List<Candle> candles =
                List.of(
                        candle(
                                "09:15:00",
                                "NIFTY",
                                "25100",
                                "25050",
                                "25000",
                                "25020",
                                100000
                        )
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(candles)
        );
    }

    @Test
    void shouldRejectCloseOutsideHighLow() {

        List<Candle> candles =
                List.of(
                        candle(
                                "09:15:00",
                                "NIFTY",
                                "25010",
                                "25050",
                                "25000",
                                "25100",
                                100000
                        )
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(candles)
        );
    }

    @Test
    void shouldRejectZeroPrice() {

        List<Candle> candles =
                List.of(
                        candle(
                                "09:15:00",
                                "NIFTY",
                                "0",
                                "25020",
                                "24990",
                                "25010",
                                100000
                        )
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(candles)
        );
    }

    @Test
    void shouldRejectNegativeVolume() {

        List<Candle> candles =
                List.of(
                        candle(
                                "09:15:00",
                                "NIFTY",
                                "25000",
                                "25020",
                                "24990",
                                "25010",
                                -1
                        )
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(candles)
        );
    }

    @Test
    void shouldRejectDuplicateTimestamp() {

        List<Candle> candles =
                List.of(
                        candle(
                                "09:15:00",
                                "NIFTY",
                                "25000",
                                "25020",
                                "24990",
                                "25010",
                                100000
                        ),
                        candle(
                                "09:15:00",
                                "NIFTY",
                                "25010",
                                "25030",
                                "25000",
                                "25025",
                                120000
                        )
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(candles)
        );
    }

    @Test
    void shouldRejectOutOfOrderCandles() {

        List<Candle> candles =
                List.of(
                        candle(
                                "09:16:00",
                                "NIFTY",
                                "25010",
                                "25030",
                                "25000",
                                "25025",
                                120000
                        ),
                        candle(
                                "09:15:00",
                                "NIFTY",
                                "25000",
                                "25020",
                                "24990",
                                "25010",
                                100000
                        )
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(candles)
        );
    }

    @Test
    void shouldRejectMultipleSymbols() {

        List<Candle> candles =
                List.of(
                        candle(
                                "09:15:00",
                                "NIFTY",
                                "25000",
                                "25020",
                                "24990",
                                "25010",
                                100000
                        ),
                        candle(
                                "09:16:00",
                                "BANKNIFTY",
                                "52000",
                                "52020",
                                "51990",
                                "52010",
                                120000
                        )
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(candles)
        );
    }

    @Test
    void shouldRejectEmptySymbol() {

        List<Candle> candles =
                List.of(
                        candle(
                                "09:15:00",
                                "",
                                "25000",
                                "25020",
                                "24990",
                                "25010",
                                100000
                        )
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(candles)
        );
    }

    @Test
    void shouldRejectNullCandle() {

        List<Candle> candles =
                Arrays.asList(
                        candle(
                                "09:15:00",
                                "NIFTY",
                                "25000",
                                "25020",
                                "24990",
                                "25010",
                                100000
                        ),
                        null
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(candles)
        );
    }

    private Candle candle(
            String time,
            String symbol,
            String open,
            String high,
            String low,
            String close,
            long volume) {

        return new Candle(
                LocalDateTime.parse(
                        "2026-09-21T" + time
                ),
                symbol,
                new BigDecimal(open),
                new BigDecimal(high),
                new BigDecimal(low),
                new BigDecimal(close),
                volume
        );
    }
}