package com.abhi.algotrading.marketdata;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MarketDataValidator {

    public void validate(List<Candle> candles) {

        if (candles == null || candles.isEmpty()) {
            throw new IllegalArgumentException(
                    "Candle data cannot be empty"
            );
        }

        Set<LocalDateTime> timestamps =
                new HashSet<>();

        String symbol =
                candles.getFirst().symbol();

        for (int i = 0; i < candles.size(); i++) {

            Candle candle = candles.get(i);

            if (candle == null) {
                throw new IllegalArgumentException(
                        "Candle cannot be null at index " + i
                );
            }

            validateSymbol(candle, symbol, i);
            validatePrices(candle, i);
            validateVolume(candle, i);
            validateTimestamp(candle, i);
            validateDuplicateTimestamp(
                    candle,
                    timestamps,
                    i
            );

            if (i > 0) {
                validateChronologicalOrder(
                        candles.get(i - 1),
                        candle,
                        i
                );
            }
        }
    }

    private void validateSymbol(
            Candle candle,
            String expectedSymbol,
            int index) {

        if (candle.symbol() == null ||
                candle.symbol().isBlank()) {

            throw new IllegalArgumentException(
                    "Symbol cannot be empty at index "
                            + index
            );
        }

        if (!candle.symbol().equals(expectedSymbol)) {

            throw new IllegalArgumentException(
                    "Multiple symbols found. Expected "
                            + expectedSymbol
                            + " but found "
                            + candle.symbol()
                            + " at index "
                            + index
            );
        }
    }

    private void validatePrices(
            Candle candle,
            int index) {

        BigDecimal open = candle.open();
        BigDecimal high = candle.high();
        BigDecimal low = candle.low();
        BigDecimal close = candle.close();

        if (open == null ||
                high == null ||
                low == null ||
                close == null) {

            throw new IllegalArgumentException(
                    "OHLC price cannot be null at index "
                            + index
            );
        }

        if (open.signum() <= 0 ||
                high.signum() <= 0 ||
                low.signum() <= 0 ||
                close.signum() <= 0) {

            throw new IllegalArgumentException(
                    "OHLC prices must be positive at index "
                            + index
            );
        }

        if (high.compareTo(low) < 0) {

            throw new IllegalArgumentException(
                    "High cannot be lower than low at index "
                            + index
            );
        }

        if (open.compareTo(low) < 0 ||
                open.compareTo(high) > 0) {

            throw new IllegalArgumentException(
                    "Open price must be between low and high at index "
                            + index
            );
        }

        if (close.compareTo(low) < 0 ||
                close.compareTo(high) > 0) {

            throw new IllegalArgumentException(
                    "Close price must be between low and high at index "
                            + index
            );
        }
    }

    private void validateVolume(
            Candle candle,
            int index) {

        if (candle.volume() < 0) {

            throw new IllegalArgumentException(
                    "Volume cannot be negative at index "
                            + index
            );
        }
    }

    private void validateTimestamp(
            Candle candle,
            int index) {

        if (candle.timestamp() == null) {

            throw new IllegalArgumentException(
                    "Timestamp cannot be null at index "
                            + index
            );
        }
    }

    private void validateDuplicateTimestamp(
            Candle candle,
            Set<LocalDateTime> timestamps,
            int index) {

        if (!timestamps.add(
                candle.timestamp()
        )) {

            throw new IllegalArgumentException(
                    "Duplicate timestamp found at index "
                            + index
                            + ": "
                            + candle.timestamp()
            );
        }
    }

    private void validateChronologicalOrder(
            Candle previous,
            Candle current,
            int index) {

        if (!current.timestamp().isAfter(
                previous.timestamp()
        )) {

            throw new IllegalArgumentException(
                    "Candles must be in chronological order. "
                            + "Invalid timestamp at index "
                            + index
            );
        }
    }
}