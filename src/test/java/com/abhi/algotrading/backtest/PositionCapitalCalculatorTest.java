package com.abhi.algotrading.backtest;

import com.abhi.algotrading.risk.PositionCapitalCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PositionCapitalCalculatorTest {

    private PositionCapitalCalculator createCalculator() {

        return new PositionCapitalCalculator();
    }

    @Test
    void shouldCalculatePositionValue() {

        PositionCapitalCalculator calculator =
                createCalculator();

        BigDecimal result =
                calculator.calculatePositionValue(
                        new BigDecimal("25050"),
                        3
                );

        assertEquals(
                new BigDecimal("75150"),
                result
        );
    }

    @Test
    void shouldCalculatePositionValueForSingleQuantity() {

        PositionCapitalCalculator calculator =
                createCalculator();

        BigDecimal result =
                calculator.calculatePositionValue(
                        new BigDecimal("25050"),
                        1
                );

        assertEquals(
                new BigDecimal("25050"),
                result
        );
    }

    @Test
    void shouldRejectNullEntryPrice() {

        PositionCapitalCalculator calculator =
                createCalculator();

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculatePositionValue(
                        null,
                        3
                )
        );
    }

    @Test
    void shouldRejectZeroEntryPrice() {

        PositionCapitalCalculator calculator =
                createCalculator();

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculatePositionValue(
                        BigDecimal.ZERO,
                        3
                )
        );
    }

    @Test
    void shouldRejectNegativeEntryPrice() {

        PositionCapitalCalculator calculator =
                createCalculator();

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculatePositionValue(
                        new BigDecimal("-25050"),
                        3
                )
        );
    }

    @Test
    void shouldRejectZeroQuantity() {

        PositionCapitalCalculator calculator =
                createCalculator();

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculatePositionValue(
                        new BigDecimal("25050"),
                        0
                )
        );
    }

    @Test
    void shouldRejectNegativeQuantity() {

        PositionCapitalCalculator calculator =
                createCalculator();

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculatePositionValue(
                        new BigDecimal("25050"),
                        -3
                )
        );
    }
}