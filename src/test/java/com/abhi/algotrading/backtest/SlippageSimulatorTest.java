package com.abhi.algotrading.backtest;

import com.abhi.algotrading.portfolio.PositionSide;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SlippageSimulatorTest {

    @Test
    void shouldIncreaseLongEntryPrice() {

        SlippageSimulator simulator =
                new SlippageSimulator(
                        new BigDecimal("0.10")
                );

        BigDecimal result =
                simulator.simulateEntry(
                        PositionSide.LONG,
                        new BigDecimal("100")
                );

        assertEquals(
                0,
                result.compareTo(
                        new BigDecimal("100.10")
                )
        );
    }

    @Test
    void shouldDecreaseShortEntryPrice() {

        SlippageSimulator simulator =
                new SlippageSimulator(
                        new BigDecimal("0.10")
                );

        BigDecimal result =
                simulator.simulateEntry(
                        PositionSide.SHORT,
                        new BigDecimal("100")
                );

        assertEquals(
                0,
                result.compareTo(
                        new BigDecimal("99.90")
                )
        );
    }

    @Test
    void shouldDecreaseLongExitPrice() {

        SlippageSimulator simulator =
                new SlippageSimulator(
                        new BigDecimal("0.10")
                );

        BigDecimal result =
                simulator.simulateExit(
                        PositionSide.LONG,
                        new BigDecimal("100")
                );

        assertEquals(
                0,
                result.compareTo(
                        new BigDecimal("99.90")
                )
        );
    }

    @Test
    void shouldIncreaseShortExitPrice() {

        SlippageSimulator simulator =
                new SlippageSimulator(
                        new BigDecimal("0.10")
                );

        BigDecimal result =
                simulator.simulateExit(
                        PositionSide.SHORT,
                        new BigDecimal("100")
                );

        assertEquals(
                0,
                result.compareTo(
                        new BigDecimal("100.10")
                )
        );
    }

    @Test
    void shouldReturnSamePriceWhenSlippageIsZero() {

        SlippageSimulator simulator =
                new SlippageSimulator(
                        BigDecimal.ZERO
                );

        BigDecimal longEntry =
                simulator.simulateEntry(
                        PositionSide.LONG,
                        new BigDecimal("100")
                );

        BigDecimal shortExit =
                simulator.simulateExit(
                        PositionSide.SHORT,
                        new BigDecimal("100")
                );

        assertEquals(
                0,
                longEntry.compareTo(
                        new BigDecimal("100")
                )
        );

        assertEquals(
                0,
                shortExit.compareTo(
                        new BigDecimal("100")
                )
        );
    }

    @Test
    void shouldRejectNegativeSlippage() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new SlippageSimulator(
                        new BigDecimal("-0.10")
                )
        );
    }

    @Test
    void shouldRejectNullSlippage() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new SlippageSimulator(null)
        );
    }

    @Test
    void shouldRejectNullPositionSide() {

        SlippageSimulator simulator =
                new SlippageSimulator(
                        new BigDecimal("0.10")
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> simulator.simulateEntry(
                        null,
                        new BigDecimal("100")
                )
        );
    }

    @Test
    void shouldRejectZeroPrice() {

        SlippageSimulator simulator =
                new SlippageSimulator(
                        new BigDecimal("0.10")
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> simulator.simulateEntry(
                        PositionSide.LONG,
                        BigDecimal.ZERO
                )
        );
    }

    @Test
    void shouldRejectNegativePrice() {

        SlippageSimulator simulator =
                new SlippageSimulator(
                        new BigDecimal("0.10")
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> simulator.simulateExit(
                        PositionSide.LONG,
                        new BigDecimal("-100")
                )
        );
    }
}