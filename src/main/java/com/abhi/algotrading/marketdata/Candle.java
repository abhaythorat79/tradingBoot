package com.abhi.algotrading.marketdata;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Candle(
        LocalDateTime timestamp,
        String symbol,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        long volume
) {
}