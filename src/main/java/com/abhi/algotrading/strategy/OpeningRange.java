package com.abhi.algotrading.strategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OpeningRange(
        LocalDateTime startTime,
        LocalDateTime endTime,
        BigDecimal high,
        BigDecimal low
) {

    public BigDecimal range() {
        return high.subtract(low);
    }
}