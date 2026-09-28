package com.abhi.algotrading.backtest;

import com.abhi.algotrading.risk.CapitalUtilizationCalculator;
import com.abhi.algotrading.risk.CapitalUtilizationConfig;
import com.abhi.algotrading.risk.PositionSizer;
import com.abhi.algotrading.risk.RiskConfig;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class RiskEdgeCaseTest {

    @Test
    void positionSizerShouldReturnZeroWhenRiskAmountIsTooSmall() {

        PositionSizer positionSizer =
                new PositionSizer();

        long quantity =
                positionSizer.calculateQuantity(
                        new BigDecimal("100"),
                        new BigDecimal("1"),
                        new BigDecimal("10000"),
                        new BigDecimal("9900")
                );

        /*
         * Risk amount = 1
         *
         * Risk per unit = 100
         *
         * 1 / 100 = 0.01
         *
         * Quantity is rounded DOWN to zero.
         */
        assertEquals(
                0,
                quantity
        );
    }

    @Test
    void positionSizerShouldHandleVerySmallStopDistance() {

        PositionSizer positionSizer =
                new PositionSizer();

        long quantity =
                positionSizer.calculateQuantity(
                        new BigDecimal("100000"),
                        new BigDecimal("1"),
                        new BigDecimal("25000"),
                        new BigDecimal("24999.99")
                );

        assertTrue(
                quantity > 0
        );
    }

    @Test
    void positionSizerShouldRejectSameEntryAndStopPrice() {

        PositionSizer positionSizer =
                new PositionSizer();

        assertThrows(
                IllegalArgumentException.class,
                () -> positionSizer.calculateQuantity(
                        new BigDecimal("100000"),
                        new BigDecimal("1"),
                        new BigDecimal("25000"),
                        new BigDecimal("25000")
                )
        );
    }

    @Test
    void capitalUtilizationShouldAllowExactlyZeroExistingCapital() {

        CapitalUtilizationCalculator calculator =
                new CapitalUtilizationCalculator(
                        new CapitalUtilizationConfig(
                                new BigDecimal("80")
                        )
                );

        assertTrue(
                calculator.canOpenPosition(
                        new BigDecimal("100000"),
                        BigDecimal.ZERO,
                        new BigDecimal("80000")
                )
        );
    }

    @Test
    void capitalUtilizationShouldBlockOneRupeeAboveLimit() {

        CapitalUtilizationCalculator calculator =
                new CapitalUtilizationCalculator(
                        new CapitalUtilizationConfig(
                                new BigDecimal("80")
                        )
                );

        assertFalse(
                calculator.canOpenPosition(
                        new BigDecimal("100000"),
                        BigDecimal.ZERO,
                        new BigDecimal("80000.01")
                )
        );
    }

    @Test
    void capitalUtilizationShouldAllow100Percent() {

        CapitalUtilizationCalculator calculator =
                new CapitalUtilizationCalculator(
                        new CapitalUtilizationConfig(
                                new BigDecimal("100")
                        )
                );

        assertTrue(
                calculator.canOpenPosition(
                        new BigDecimal("100000"),
                        BigDecimal.ZERO,
                        new BigDecimal("100000")
                )
        );
    }

    @Test
    void capitalUtilizationShouldBlockAbove100Percent() {

        CapitalUtilizationCalculator calculator =
                new CapitalUtilizationCalculator(
                        new CapitalUtilizationConfig(
                                new BigDecimal("100")
                        )
                );

        assertFalse(
                calculator.canOpenPosition(
                        new BigDecimal("100000"),
                        BigDecimal.ZERO,
                        new BigDecimal("100000.01")
                )
        );
    }

    @Test
    void riskConfigShouldRejectZeroCapital() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new RiskConfig(
                        BigDecimal.ZERO,
                        new BigDecimal("1"),
                        new BigDecimal("1000"),
                        5,
                        3,
                        new BigDecimal("1"),
                        new BigDecimal("2"),
                        new BigDecimal("1")
                )
        );
    }

    @Test
    void riskConfigShouldRejectZeroRiskPerTrade() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new RiskConfig(
                        new BigDecimal("100000"),
                        BigDecimal.ZERO,
                        new BigDecimal("1000"),
                        5,
                        3,
                        new BigDecimal("1"),
                        new BigDecimal("2"),
                        new BigDecimal("1")
                )
        );
    }

    @Test
    void riskConfigShouldRejectZeroDailyLossLimit() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new RiskConfig(
                        new BigDecimal("100000"),
                        new BigDecimal("1"),
                        BigDecimal.ZERO,
                        5,
                        3,
                        new BigDecimal("1"),
                        new BigDecimal("2"),
                        new BigDecimal("1")
                )
        );
    }

    @Test
    void riskConfigShouldRejectZeroMaximumTrades() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new RiskConfig(
                        new BigDecimal("100000"),
                        new BigDecimal("1"),
                        new BigDecimal("1000"),
                        0,
                        3,
                        new BigDecimal("1"),
                        new BigDecimal("2"),
                        new BigDecimal("1")
                )
        );
    }

    @Test
    void riskConfigShouldRejectZeroMaximumConsecutiveLosses() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new RiskConfig(
                        new BigDecimal("100000"),
                        new BigDecimal("1"),
                        new BigDecimal("1000"),
                        5,
                        0,
                        new BigDecimal("1"),
                        new BigDecimal("2"),
                        new BigDecimal("1")
                )
        );
    }

    @Test
    void riskConfigShouldRejectZeroStopLossPercent() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new RiskConfig(
                        new BigDecimal("100000"),
                        new BigDecimal("1"),
                        new BigDecimal("1000"),
                        5,
                        3,
                        BigDecimal.ZERO,
                        new BigDecimal("2"),
                        new BigDecimal("1")
                )
        );
    }

    @Test
    void riskConfigShouldRejectZeroTargetPercent() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new RiskConfig(
                        new BigDecimal("100000"),
                        new BigDecimal("1"),
                        new BigDecimal("1000"),
                        5,
                        3,
                        new BigDecimal("1"),
                        BigDecimal.ZERO,
                        new BigDecimal("1")
                )
        );
    }

    @Test
    void riskConfigShouldRejectZeroTrailingStopPercent() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new RiskConfig(
                        new BigDecimal("100000"),
                        new BigDecimal("1"),
                        new BigDecimal("1000"),
                        5,
                        3,
                        new BigDecimal("1"),
                        new BigDecimal("2"),
                        BigDecimal.ZERO
                )
        );
    }
}