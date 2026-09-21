package com.abhi.algotrading.risk;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PositionSizerTest {

    private final PositionSizer positionSizer =
            new PositionSizer();

    @Test
    void shouldCalculateQuantityBasedOnRisk() {

        long quantity =
                positionSizer.calculateQuantity(
                        new BigDecimal("100000"),
                        new BigDecimal("1"),
                        new BigDecimal("100"),
                        new BigDecimal("98")
                );

        assertEquals(
                500,
                quantity
        );
    }

    @Test
    void shouldRoundQuantityDown() {

        long quantity =
                positionSizer.calculateQuantity(
                        new BigDecimal("100000"),
                        new BigDecimal("1"),
                        new BigDecimal("100"),
                        new BigDecimal("97.5")
                );

        assertEquals(
                400,
                quantity
        );
    }
}