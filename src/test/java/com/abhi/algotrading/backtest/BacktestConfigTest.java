package com.abhi.algotrading.backtest;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BacktestConfigTest {

    @Test
    void shouldCreateValidConfiguration() {

        BacktestConfig config =
                new BacktestConfig(
                        new BigDecimal("100000"),
                        new BigDecimal("1"),
                        new BigDecimal("5000"),
                        3,
                        3,
                        new BigDecimal("1"),
                        new BigDecimal("2"),
                        new BigDecimal("1")
                );

        assertEquals(
                0,
                config.initialCapital()
                        .compareTo(
                                new BigDecimal("100000")
                        )
        );

        assertEquals(
                0,
                config.riskPerTradePercent()
                        .compareTo(
                                new BigDecimal("1")
                        )
        );

        assertEquals(3, config.maxTradesPerDay());
        assertEquals(3, config.maxConsecutiveLosses());
    }

    @Test
    void shouldRejectInvalidCapital() {

        assertThrows(
                IllegalArgumentException.class,
                () -> createConfig(
                        "0",
                        "1",
                        "5000",
                        3,
                        3,
                        "1",
                        "2",
                        "1"
                )
        );
    }

    @Test
    void shouldRejectInvalidRiskPercentage() {

        assertThrows(
                IllegalArgumentException.class,
                () -> createConfig(
                        "100000",
                        "0",
                        "5000",
                        3,
                        3,
                        "1",
                        "2",
                        "1"
                )
        );
    }

    @Test
    void shouldRejectInvalidDailyLoss() {

        assertThrows(
                IllegalArgumentException.class,
                () -> createConfig(
                        "100000",
                        "1",
                        "0",
                        3,
                        3,
                        "1",
                        "2",
                        "1"
                )
        );
    }

    @Test
    void shouldRejectInvalidMaxTrades() {

        assertThrows(
                IllegalArgumentException.class,
                () -> createConfig(
                        "100000",
                        "1",
                        "5000",
                        0,
                        3,
                        "1",
                        "2",
                        "1"
                )
        );
    }

    @Test
    void shouldRejectInvalidMaxConsecutiveLosses() {

        assertThrows(
                IllegalArgumentException.class,
                () -> createConfig(
                        "100000",
                        "1",
                        "5000",
                        3,
                        0,
                        "1",
                        "2",
                        "1"
                )
        );
    }

    @Test
    void shouldRejectInvalidStopLoss() {

        assertThrows(
                IllegalArgumentException.class,
                () -> createConfig(
                        "100000",
                        "1",
                        "5000",
                        3,
                        3,
                        "0",
                        "2",
                        "1"
                )
        );
    }

    @Test
    void shouldRejectInvalidTarget() {

        assertThrows(
                IllegalArgumentException.class,
                () -> createConfig(
                        "100000",
                        "1",
                        "5000",
                        3,
                        3,
                        "1",
                        "0",
                        "1"
                )
        );
    }

    @Test
    void shouldRejectInvalidTrailingStop() {

        assertThrows(
                IllegalArgumentException.class,
                () -> createConfig(
                        "100000",
                        "1",
                        "5000",
                        3,
                        3,
                        "1",
                        "2",
                        "0"
                )
        );
    }

    private BacktestConfig createConfig(
            String capital,
            String risk,
            String dailyLoss,
            int maxTrades,
            int maxLosses,
            String stopLoss,
            String target,
            String trailingStop) {

        return new BacktestConfig(
                new BigDecimal(capital),
                new BigDecimal(risk),
                new BigDecimal(dailyLoss),
                maxTrades,
                maxLosses,
                new BigDecimal(stopLoss),
                new BigDecimal(target),
                new BigDecimal(trailingStop)
        );
    }
}