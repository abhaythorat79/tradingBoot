package com.abhi.algotrading.backtest;

import com.abhi.algotrading.portfolio.PositionSide;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BacktestTrade(
        String symbol,
        PositionSide side,
        long quantity,
        BigDecimal entryPrice,
        BigDecimal exitPrice,
        LocalDateTime entryTime,
        LocalDateTime exitTime,
        BigDecimal grossPnl,
        String exitReason
) {

    public BacktestTrade {

        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException(
                    "Symbol cannot be empty"
            );
        }

        if (side == null) {
            throw new IllegalArgumentException(
                    "Position side cannot be null"
            );
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        if (entryPrice == null ||
                entryPrice.signum() <= 0) {

            throw new IllegalArgumentException(
                    "Entry price must be positive"
            );
        }

        if (exitPrice == null ||
                exitPrice.signum() <= 0) {

            throw new IllegalArgumentException(
                    "Exit price must be positive"
            );
        }

        if (entryTime == null) {
            throw new IllegalArgumentException(
                    "Entry time cannot be null"
            );
        }

        if (exitTime == null) {
            throw new IllegalArgumentException(
                    "Exit time cannot be null"
            );
        }

        if (grossPnl == null) {
            throw new IllegalArgumentException(
                    "Gross P&L cannot be null"
            );
        }

        if (exitReason == null ||
                exitReason.isBlank()) {

            throw new IllegalArgumentException(
                    "Exit reason cannot be empty"
            );
        }
    }
}