package com.abhi.algotrading.backtest;

import com.abhi.algotrading.portfolio.PositionSide;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BacktestTradeTest {

    @Test
    void shouldCreateValidBacktestTrade() {

        BacktestTrade trade =
                new BacktestTrade(
                        "NIFTY",
                        PositionSide.LONG,
                        100,
                        new BigDecimal("25000"),
                        new BigDecimal("25500"),
                        LocalDateTime.of(
                                2026,
                                9,
                                21,
                                9,
                                45
                        ),
                        LocalDateTime.of(
                                2026,
                                9,
                                21,
                                10,
                                15
                        ),
                        new BigDecimal("50000"),
                        "TARGET"
                );

        assertEquals("NIFTY", trade.symbol());
        assertEquals(
                PositionSide.LONG,
                trade.side()
        );
        assertEquals(100, trade.quantity());

        assertEquals(
                0,
                trade.entryPrice().compareTo(
                        new BigDecimal("25000")
                )
        );

        assertEquals(
                0,
                trade.exitPrice().compareTo(
                        new BigDecimal("25500")
                )
        );

        assertEquals(
                0,
                trade.grossPnl().compareTo(
                        new BigDecimal("50000")
                )
        );

        assertEquals(
                "TARGET",
                trade.exitReason()
        );
    }

    @Test
    void shouldRejectEmptySymbol() {

        assertThrows(
                IllegalArgumentException.class,
                () -> createTrade(
                        "",
                        PositionSide.LONG,
                        100
                )
        );
    }

    @Test
    void shouldRejectNullSide() {

        assertThrows(
                IllegalArgumentException.class,
                () -> createTrade(
                        "NIFTY",
                        null,
                        100
                )
        );
    }

    @Test
    void shouldRejectInvalidQuantity() {

        assertThrows(
                IllegalArgumentException.class,
                () -> createTrade(
                        "NIFTY",
                        PositionSide.LONG,
                        0
                )
        );
    }

    @Test
    void shouldRejectInvalidEntryPrice() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new BacktestTrade(
                        "NIFTY",
                        PositionSide.LONG,
                        100,
                        BigDecimal.ZERO,
                        new BigDecimal("25500"),
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        new BigDecimal("50000"),
                        "TARGET"
                )
        );
    }

    @Test
    void shouldRejectInvalidExitPrice() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new BacktestTrade(
                        "NIFTY",
                        PositionSide.LONG,
                        100,
                        new BigDecimal("25000"),
                        BigDecimal.ZERO,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        new BigDecimal("50000"),
                        "TARGET"
                )
        );
    }

    @Test
    void shouldRejectNullEntryTime() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new BacktestTrade(
                        "NIFTY",
                        PositionSide.LONG,
                        100,
                        new BigDecimal("25000"),
                        new BigDecimal("25500"),
                        null,
                        LocalDateTime.now(),
                        new BigDecimal("50000"),
                        "TARGET"
                )
        );
    }

    @Test
    void shouldRejectNullExitTime() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new BacktestTrade(
                        "NIFTY",
                        PositionSide.LONG,
                        100,
                        new BigDecimal("25000"),
                        new BigDecimal("25500"),
                        LocalDateTime.now(),
                        null,
                        new BigDecimal("50000"),
                        "TARGET"
                )
        );
    }

    @Test
    void shouldRejectNullGrossPnl() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new BacktestTrade(
                        "NIFTY",
                        PositionSide.LONG,
                        100,
                        new BigDecimal("25000"),
                        new BigDecimal("25500"),
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        null,
                        "TARGET"
                )
        );
    }

    @Test
    void shouldRejectEmptyExitReason() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new BacktestTrade(
                        "NIFTY",
                        PositionSide.LONG,
                        100,
                        new BigDecimal("25000"),
                        new BigDecimal("25500"),
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        new BigDecimal("50000"),
                        ""
                )
        );
    }

    private BacktestTrade createTrade(
            String symbol,
            PositionSide side,
            long quantity) {

        return new BacktestTrade(
                symbol,
                side,
                quantity,
                new BigDecimal("25000"),
                new BigDecimal("25500"),
                LocalDateTime.of(
                        2026,
                        9,
                        21,
                        9,
                        45
                ),
                LocalDateTime.of(
                        2026,
                        9,
                        21,
                        10,
                        15
                ),
                new BigDecimal("50000"),
                "TARGET"
        );
    }
}