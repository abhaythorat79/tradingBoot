package com.abhi.algotrading.backtest;

import com.abhi.algotrading.risk.CapitalUtilizationCalculator;
import com.abhi.algotrading.risk.CapitalUtilizationConfig;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CapitalUtilizationCalculatorTest {

    private CapitalUtilizationCalculator createCalculator() {

        CapitalUtilizationConfig config =
                new CapitalUtilizationConfig(
                        new BigDecimal("80")
                );

        return new CapitalUtilizationCalculator(
                config
        );
    }

    @Test
    void shouldCalculateMaximumAllowedCapital() {

        CapitalUtilizationCalculator calculator =
                createCalculator();

        BigDecimal result =
                calculator.calculateMaximumAllowedCapital(
                        new BigDecimal("100000")
                );

        assertEquals(
                new BigDecimal("80000.0000000000"),
                result
        );
    }

    @Test
    void shouldCalculateUtilizedCapital() {

        CapitalUtilizationCalculator calculator =
                createCalculator();

        BigDecimal result =
                calculator.calculateUtilizedCapital(
                        new BigDecimal("50000"),
                        new BigDecimal("20000")
                );

        assertEquals(
                new BigDecimal("70000"),
                result
        );
    }

    @Test
    void shouldAllowPositionWithinUtilizationLimit() {

        CapitalUtilizationCalculator calculator =
                createCalculator();

        boolean allowed =
                calculator.canOpenPosition(
                        new BigDecimal("100000"),
                        new BigDecimal("50000"),
                        new BigDecimal("20000")
                );

        assertTrue(allowed);
    }

    @Test
    void shouldAllowPositionExactlyAtUtilizationLimit() {

        CapitalUtilizationCalculator calculator =
                createCalculator();

        boolean allowed =
                calculator.canOpenPosition(
                        new BigDecimal("100000"),
                        new BigDecimal("60000"),
                        new BigDecimal("20000")
                );

        assertTrue(allowed);
    }

    @Test
    void shouldBlockPositionAboveUtilizationLimit() {

        CapitalUtilizationCalculator calculator =
                createCalculator();

        boolean allowed =
                calculator.canOpenPosition(
                        new BigDecimal("100000"),
                        new BigDecimal("70000"),
                        new BigDecimal("20000")
                );

        assertFalse(allowed);
    }

    @Test
    void shouldCalculateUtilizationPercentage() {

        CapitalUtilizationCalculator calculator =
                createCalculator();

        BigDecimal result =
                calculator.calculateUtilizationPercent(
                        new BigDecimal("100000"),
                        new BigDecimal("70000")
                );

        assertEquals(
                new BigDecimal("70.0000000000"),
                result
        );
    }

    @Test
    void shouldRejectInvalidTotalCapital() {

        CapitalUtilizationCalculator calculator =
                createCalculator();

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculateMaximumAllowedCapital(
                        BigDecimal.ZERO
                )
        );
    }

    @Test
    void shouldRejectNegativeCommittedCapital() {

        CapitalUtilizationCalculator calculator =
                createCalculator();

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculateUtilizedCapital(
                        new BigDecimal("-1"),
                        new BigDecimal("10000")
                )
        );
    }

    @Test
    void shouldRejectNegativeProposedPositionValue() {

        CapitalUtilizationCalculator calculator =
                createCalculator();

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculateUtilizedCapital(
                        new BigDecimal("10000"),
                        new BigDecimal("-1")
                )
        );
    }
}