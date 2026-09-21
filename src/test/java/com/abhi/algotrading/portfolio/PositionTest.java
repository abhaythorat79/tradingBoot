package com.abhi.algotrading.portfolio;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PositionTest {

    private final LocalDateTime entryTime =
            LocalDateTime.parse(
                    "2026-09-18T09:35"
            );

    @Test
    void shouldCreateLongPosition() {

        Position position =
                new Position(
                        "TEST",
                        PositionSide.LONG,
                        new BigDecimal("100"),
                        10,
                        new BigDecimal("98"),
                        new BigDecimal("102"),
                        entryTime
                );

        assertEquals(
                PositionSide.LONG,
                position.getSide()
        );

        assertEquals(
                new BigDecimal("100"),
                position.getEntryPrice()
        );

        assertEquals(
                new BigDecimal("98"),
                position.getCurrentStopLoss()
        );

        assertEquals(
                new BigDecimal("102"),
                position.getCurrentTarget()
        );

        assertTrue(
                position.isOpen()
        );
    }

    @Test
    void shouldCreateShortPosition() {

        Position position =
                new Position(
                        "TEST",
                        PositionSide.SHORT,
                        new BigDecimal("100"),
                        10,
                        new BigDecimal("102"),
                        new BigDecimal("98"),
                        entryTime
                );

        assertEquals(
                PositionSide.SHORT,
                position.getSide()
        );

        assertEquals(
                new BigDecimal("102"),
                position.getCurrentStopLoss()
        );

        assertEquals(
                new BigDecimal("98"),
                position.getCurrentTarget()
        );
    }

    @Test
    void shouldConvertLongTargetIntoStopLoss() {

        Position position =
                new Position(
                        "TEST",
                        PositionSide.LONG,
                        new BigDecimal("100"),
                        10,
                        new BigDecimal("98"),
                        new BigDecimal("102"),
                        entryTime
                );

        position.targetReached();

        assertEquals(
                new BigDecimal("102"),
                position.getCurrentStopLoss()
        );

        assertNull(
                position.getCurrentTarget()
        );

        assertFalse(
                position.hasFixedTarget()
        );
    }

    @Test
    void shouldConvertShortTargetIntoStopLoss() {

        Position position =
                new Position(
                        "TEST",
                        PositionSide.SHORT,
                        new BigDecimal("100"),
                        10,
                        new BigDecimal("102"),
                        new BigDecimal("98"),
                        entryTime
                );

        position.targetReached();

        assertEquals(
                new BigDecimal("98"),
                position.getCurrentStopLoss()
        );

        assertNull(
                position.getCurrentTarget()
        );
    }

    @Test
    void longTrailingStopShouldOnlyMoveUp() {

        Position position =
                new Position(
                        "TEST",
                        PositionSide.LONG,
                        new BigDecimal("100"),
                        10,
                        new BigDecimal("98"),
                        new BigDecimal("102"),
                        entryTime
                );

        position.targetReached();

        // Move SL from 102 → 103
        position.updateTrailingStop(
                new BigDecimal("103")
        );

        assertEquals(
                new BigDecimal("103"),
                position.getCurrentStopLoss()
        );

        // Try moving SL backward from 103 → 101
        position.updateTrailingStop(
                new BigDecimal("101")
        );

        // It must remain 103
        assertEquals(
                new BigDecimal("103"),
                position.getCurrentStopLoss()
        );
    }

    @Test
    void shortTrailingStopShouldOnlyMoveDown() {

        Position position =
                new Position(
                        "TEST",
                        PositionSide.SHORT,
                        new BigDecimal("100"),
                        10,
                        new BigDecimal("102"),
                        new BigDecimal("98"),
                        entryTime
                );

        position.targetReached();

        // Move SL from 98 → 97
        position.updateTrailingStop(
                new BigDecimal("97")
        );

        assertEquals(
                new BigDecimal("97"),
                position.getCurrentStopLoss()
        );

        // Try moving SL backward from 97 → 99
        position.updateTrailingStop(
                new BigDecimal("99")
        );

        // It must remain 97
        assertEquals(
                new BigDecimal("97"),
                position.getCurrentStopLoss()
        );
    }

    @Test
    void shouldDetectLongStopLossHit() {

        Position position =
                new Position(
                        "TEST",
                        PositionSide.LONG,
                        new BigDecimal("100"),
                        10,
                        new BigDecimal("98"),
                        new BigDecimal("102"),
                        entryTime
                );

        assertTrue(
                position.isStopLossHit(
                        new BigDecimal("98")
                )
        );

        assertTrue(
                position.isStopLossHit(
                        new BigDecimal("97")
                )
        );

        assertFalse(
                position.isStopLossHit(
                        new BigDecimal("99")
                )
        );
    }

    @Test
    void shouldDetectShortStopLossHit() {

        Position position =
                new Position(
                        "TEST",
                        PositionSide.SHORT,
                        new BigDecimal("100"),
                        10,
                        new BigDecimal("102"),
                        new BigDecimal("98"),
                        entryTime
                );

        assertTrue(
                position.isStopLossHit(
                        new BigDecimal("102")
                )
        );

        assertTrue(
                position.isStopLossHit(
                        new BigDecimal("103")
                )
        );

        assertFalse(
                position.isStopLossHit(
                        new BigDecimal("101")
                )
        );
    }

    @Test
    void shouldCalculateLongUnrealizedPnl() {

        Position position =
                new Position(
                        "TEST",
                        PositionSide.LONG,
                        new BigDecimal("100"),
                        10,
                        new BigDecimal("98"),
                        new BigDecimal("102"),
                        entryTime
                );

        BigDecimal pnl =
                position.unrealizedPnl(
                        new BigDecimal("105")
                );

        assertEquals(
                new BigDecimal("50"),
                pnl
        );
    }

    @Test
    void shouldCalculateShortUnrealizedPnl() {

        Position position =
                new Position(
                        "TEST",
                        PositionSide.SHORT,
                        new BigDecimal("100"),
                        10,
                        new BigDecimal("102"),
                        new BigDecimal("98"),
                        entryTime
                );

        BigDecimal pnl =
                position.unrealizedPnl(
                        new BigDecimal("95")
                );

        assertEquals(
                new BigDecimal("50"),
                pnl
        );
    }

    @Test
    void shouldClosePosition() {

        Position position =
                new Position(
                        "TEST",
                        PositionSide.LONG,
                        new BigDecimal("100"),
                        10,
                        new BigDecimal("98"),
                        new BigDecimal("102"),
                        entryTime
                );

        LocalDateTime exitTime =
                LocalDateTime.parse(
                        "2026-09-18T10:00"
                );

        position.close(
                new BigDecimal("105"),
                exitTime
        );

        assertFalse(
                position.isOpen()
        );

        assertEquals(
                PositionStatus.CLOSED,
                position.getStatus()
        );

        assertEquals(
                new BigDecimal("105"),
                position.getExitPrice()
        );

        assertEquals(
                exitTime,
                position.getExitTime()
        );
    }
}