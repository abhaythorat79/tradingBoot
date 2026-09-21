package com.abhi.algotrading.risk;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class RiskManagerTest {

    private RiskManager createRiskManager() {

        RiskConfig config =
                new RiskConfig(
                        new BigDecimal("100000"),
                        new BigDecimal("1"),
                        new BigDecimal("2000"),
                        3,
                        2,
                        new BigDecimal("2"),
                        new BigDecimal("4"),
                        new BigDecimal("1")
                );

        return new RiskManager(config);
    }

    @Test
    void shouldAllowTradeInitially() {

        RiskManager manager =
                createRiskManager();

        assertTrue(
                manager.canOpenNewTrade()
        );
    }

    @Test
    void shouldRejectTradeAfterMaximumTrades() {

        RiskManager manager =
                createRiskManager();

        manager.recordTrade();
        manager.recordTrade();
        manager.recordTrade();

        assertFalse(
                manager.canOpenNewTrade()
        );
    }

    @Test
    void shouldRejectTradeAfterMaximumConsecutiveLosses() {

        RiskManager manager =
                createRiskManager();

        manager.recordTradeResult(
                new BigDecimal("-500")
        );

        manager.recordTradeResult(
                new BigDecimal("-500")
        );

        assertEquals(
                2,
                manager.getConsecutiveLosses()
        );

        assertFalse(
                manager.canOpenNewTrade()
        );
    }

    @Test
    void winningTradeShouldResetConsecutiveLosses() {

        RiskManager manager =
                createRiskManager();

        manager.recordTradeResult(
                new BigDecimal("-500")
        );

        manager.recordTradeResult(
                new BigDecimal("-300")
        );

        manager.recordTradeResult(
                new BigDecimal("800")
        );

        assertEquals(
                0,
                manager.getConsecutiveLosses()
        );
    }

    @Test
    void shouldRejectTradeAfterDailyLossLimit() {

        RiskManager manager =
                createRiskManager();

        manager.recordTradeResult(
                new BigDecimal("-2000")
        );

        assertEquals(
                new BigDecimal("-2000"),
                manager.getDailyPnl()
        );

        assertFalse(
                manager.canOpenNewTrade()
        );
    }

    @Test
    void resetShouldClearDailyState() {

        RiskManager manager =
                createRiskManager();

        manager.recordTrade();

        manager.recordTradeResult(
                new BigDecimal("-500")
        );

        manager.resetDailyState();

        assertEquals(
                0,
                manager.getTradesToday()
        );

        assertEquals(
                0,
                manager.getConsecutiveLosses()
        );

        assertEquals(
                BigDecimal.ZERO,
                manager.getDailyPnl()
        );

        assertTrue(
                manager.canOpenNewTrade()
        );
    }
}