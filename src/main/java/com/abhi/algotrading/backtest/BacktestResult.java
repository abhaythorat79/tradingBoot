package com.abhi.algotrading.backtest;

import java.math.BigDecimal;
import java.util.List;

public class BacktestResult {

    private final int tradingDays;
    private final List<BacktestTrade> trades;

    public BacktestResult(
            int tradingDays,
            List<BacktestTrade> trades) {

        if (tradingDays < 0) {
            throw new IllegalArgumentException(
                    "Trading days cannot be negative"
            );
        }

        if (trades == null) {
            throw new IllegalArgumentException(
                    "Trades cannot be null"
            );
        }

        this.tradingDays = tradingDays;
        this.trades = List.copyOf(trades);
    }

    public int getTradingDays() {
        return tradingDays;
    }

    public List<BacktestTrade> getTrades() {
        return trades;
    }

    public int getTotalTrades() {
        return trades.size();
    }

    public int getWinningTrades() {

        return (int) trades.stream()
                .filter(trade ->
                        trade.grossPnl()
                                .compareTo(BigDecimal.ZERO) > 0
                )
                .count();
    }

    public int getLosingTrades() {

        return (int) trades.stream()
                .filter(trade ->
                        trade.grossPnl()
                                .compareTo(BigDecimal.ZERO) < 0
                )
                .count();
    }

    public int getBreakevenTrades() {

        return (int) trades.stream()
                .filter(trade ->
                        trade.grossPnl()
                                .compareTo(BigDecimal.ZERO) == 0
                )
                .count();
    }

    public BigDecimal getGrossPnl() {

        return trades.stream()
                .map(BacktestTrade::grossPnl)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }
}