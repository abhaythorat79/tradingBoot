package com.abhi.algotrading.backtest;

import com.abhi.algotrading.risk.CapitalUtilizationConfig;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CapitalUtilizationConfigTest {

    @Test
    void shouldCreateValidCapitalUtilizationConfig() {

        CapitalUtilizationConfig config =
                new CapitalUtilizationConfig(
                        new BigDecimal("80")
                );

        assertEquals(
                new BigDecimal("80"),
                config.maximumUtilizationPercent()
        );
    }

    @Test
    void shouldRejectNullUtilizationPercent() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new CapitalUtilizationConfig(null)
        );
    }

    @Test
    void shouldRejectZeroUtilizationPercent() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new CapitalUtilizationConfig(
                        BigDecimal.ZERO
                )
        );
    }

    @Test
    void shouldRejectNegativeUtilizationPercent() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new CapitalUtilizationConfig(
                        new BigDecimal("-10")
                )
        );
    }

    @Test
    void shouldRejectUtilizationAbove100Percent() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new CapitalUtilizationConfig(
                        new BigDecimal("100.01")
                )
        );
    }

    @Test
    void shouldAllowExactly100Percent() {

        CapitalUtilizationConfig config =
                new CapitalUtilizationConfig(
                        new BigDecimal("100")
                );

        assertEquals(
                new BigDecimal("100"),
                config.maximumUtilizationPercent()
        );
    }
}