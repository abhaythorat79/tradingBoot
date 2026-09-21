package com.abhi.algotrading.risk;

import java.math.BigDecimal;

public class RiskManager {

    private final RiskConfig config;

    private int tradesToday;

    private int consecutiveLosses;

    private BigDecimal dailyPnl =
            BigDecimal.ZERO;

    public RiskManager(RiskConfig config) {

        if (config == null) {
            throw new IllegalArgumentException(
                    "Risk configuration cannot be null"
            );
        }

        this.config = config;
    }

    public boolean canOpenNewTrade() {

        if (tradesToday >=
                config.maxTradesPerDay()) {

            return false;
        }

        if (consecutiveLosses >=
                config.maxConsecutiveLosses()) {

            return false;
        }

        if (dailyPnl.compareTo(
                config.maxDailyLoss().negate()
        ) <= 0) {

            return false;
        }

        return true;
    }

    public void recordTrade() {

        tradesToday++;
    }

    public void recordTradeResult(
            BigDecimal pnl) {

        if (pnl == null) {
            throw new IllegalArgumentException(
                    "P&L cannot be null"
            );
        }

        dailyPnl =
                dailyPnl.add(pnl);

        if (pnl.signum() < 0) {
            consecutiveLosses++;
        } else if (pnl.signum() > 0) {
            consecutiveLosses = 0;
        }
    }

    public void resetDailyState() {

        tradesToday = 0;

        consecutiveLosses = 0;

        dailyPnl = BigDecimal.ZERO;
    }

    public int getTradesToday() {
        return tradesToday;
    }

    public int getConsecutiveLosses() {
        return consecutiveLosses;
    }

    public BigDecimal getDailyPnl() {
        return dailyPnl;
    }

    public RiskConfig getConfig() {
        return config;
    }
}