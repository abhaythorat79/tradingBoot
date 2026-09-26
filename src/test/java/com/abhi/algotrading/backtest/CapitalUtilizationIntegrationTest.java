package com.abhi.algotrading.backtest;

import com.abhi.algotrading.risk.CapitalUtilizationCalculator;
import com.abhi.algotrading.risk.CapitalUtilizationConfig;
import com.abhi.algotrading.risk.PositionCapitalCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CapitalUtilizationIntegrationTest {

    private static final BigDecimal CAPITAL =
            new BigDecimal("100000");

    private static final BigDecimal MAX_UTILIZATION =
            new BigDecimal("80");

    private CapitalUtilizationCalculator
    createUtilizationCalculator() {

        CapitalUtilizationConfig config =
                new CapitalUtilizationConfig(
                        MAX_UTILIZATION
                );

        return new CapitalUtilizationCalculator(
                config
        );
    }

    @Test
    void shouldAllowPositionWhenCapitalUtilizationIsWithinLimit() {

        PositionCapitalCalculator positionCapitalCalculator =
                new PositionCapitalCalculator();

        CapitalUtilizationCalculator utilizationCalculator =
                createUtilizationCalculator();

        BigDecimal positionValue =
                positionCapitalCalculator.calculatePositionValue(
                        new BigDecimal("25050"),
                        3
                );

        /*
         * Position value:
         *
         * 25,050 × 3 = 75,150
         */

        assertEquals(
                new BigDecimal("75150"),
                positionValue
        );

        boolean allowed =
                utilizationCalculator.canOpenPosition(
                        CAPITAL,
                        BigDecimal.ZERO,
                        positionValue
                );

        assertTrue(allowed);
    }

    @Test
    void shouldBlockPositionWhenCapitalUtilizationExceedsLimit() {

        PositionCapitalCalculator positionCapitalCalculator =
                new PositionCapitalCalculator();

        CapitalUtilizationCalculator utilizationCalculator =
                createUtilizationCalculator();

        /*
         * Entry = 30,000
         * Quantity = 3
         *
         * Position value = 90,000
         *
         * Maximum allowed = 80,000
         *
         * Therefore the position must be blocked.
         */

        BigDecimal positionValue =
                positionCapitalCalculator.calculatePositionValue(
                        new BigDecimal("30000"),
                        3
                );

        assertEquals(
                new BigDecimal("90000"),
                positionValue
        );

        boolean allowed =
                utilizationCalculator.canOpenPosition(
                        CAPITAL,
                        BigDecimal.ZERO,
                        positionValue
                );

        assertFalse(allowed);
    }

    @Test
    void shouldAllowPositionExactlyAtMaximumUtilization() {

        PositionCapitalCalculator positionCapitalCalculator =
                new PositionCapitalCalculator();

        CapitalUtilizationCalculator utilizationCalculator =
                createUtilizationCalculator();

        /*
         * Maximum allowed capital:
         *
         * 100,000 × 80% = 80,000
         *
         * Position:
         *
         * 20,000 × 4 = 80,000
         */

        BigDecimal positionValue =
                positionCapitalCalculator.calculatePositionValue(
                        new BigDecimal("20000"),
                        4
                );

        assertEquals(
                new BigDecimal("80000"),
                positionValue
        );

        boolean allowed =
                utilizationCalculator.canOpenPosition(
                        CAPITAL,
                        BigDecimal.ZERO,
                        positionValue
                );

        assertTrue(allowed);
    }

    @Test
    void shouldConsiderExistingCommittedCapital() {

        PositionCapitalCalculator positionCapitalCalculator =
                new PositionCapitalCalculator();

        CapitalUtilizationCalculator utilizationCalculator =
                createUtilizationCalculator();

        /*
         * Existing committed capital:
         * 60,000
         *
         * New position:
         * 15,000
         *
         * Total:
         * 75,000
         *
         * Maximum:
         * 80,000
         *
         * Trade should be allowed.
         */

        BigDecimal newPositionValue =
                positionCapitalCalculator.calculatePositionValue(
                        new BigDecimal("5000"),
                        3
                );

        assertEquals(
                new BigDecimal("15000"),
                newPositionValue
        );

        boolean allowed =
                utilizationCalculator.canOpenPosition(
                        CAPITAL,
                        new BigDecimal("60000"),
                        newPositionValue
                );

        assertTrue(allowed);
    }

    @Test
    void shouldBlockWhenExistingCapitalPlusNewPositionExceedsLimit() {

        PositionCapitalCalculator positionCapitalCalculator =
                new PositionCapitalCalculator();

        CapitalUtilizationCalculator utilizationCalculator =
                createUtilizationCalculator();

        /*
         * Existing committed capital:
         * 70,000
         *
         * New position:
         * 15,000
         *
         * Total:
         * 85,000
         *
         * Maximum:
         * 80,000
         *
         * Trade must be blocked.
         */

        BigDecimal newPositionValue =
                positionCapitalCalculator.calculatePositionValue(
                        new BigDecimal("5000"),
                        3
                );

        assertEquals(
                new BigDecimal("15000"),
                newPositionValue
        );

        boolean allowed =
                utilizationCalculator.canOpenPosition(
                        CAPITAL,
                        new BigDecimal("70000"),
                        newPositionValue
                );

        assertFalse(allowed);
    }
}