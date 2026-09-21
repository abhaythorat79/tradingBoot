package com.abhi.algotrading.risk;

import com.abhi.algotrading.portfolio.Position;
import com.abhi.algotrading.portfolio.PositionSide;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrailingStopManagerTest {

    private final TrailingStopManager manager =
            new TrailingStopManager(
                    new BigDecimal("1")
            );

    private final LocalDateTime entryTime =
            LocalDateTime.parse(
                    "2026-09-18T09:35"
            );


    @Test
    void shouldNotTrailLongPositionBeforeTarget() {

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

        manager.update(
                position,
                new BigDecimal("105")
        );

        /*
         * Target has NOT been reached.
         *
         * SL must remain 98.
         */
        assertEquals(
                new BigDecimal("98"),
                position.getCurrentStopLoss()
        );
    }


    @Test
    void shouldTrailLongPositionAfterTarget() {

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

        /*
         * Target reached.
         *
         * 102 becomes the new SL.
         */
        position.targetReached();

        manager.update(
                position,
                new BigDecimal("105")
        );

        /*
         * 1% trailing:
         *
         * 105 - 1% = 103.95
         */
        assertEquals(
                new BigDecimal("103.9500000000"),
                position.getCurrentStopLoss()
        );
    }


    @Test
    void shouldNotMoveLongStopBackward() {

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

        /*
         * Price 105
         * Trailing SL = 103.95
         */
        manager.update(
                position,
                new BigDecimal("105")
        );

        /*
         * Price falls to 103.
         *
         * Calculated trailing SL would be 101.97,
         * but that is lower than current SL.
         *
         * Therefore SL must remain 103.95.
         */
        manager.update(
                position,
                new BigDecimal("103")
        );

        assertEquals(
                new BigDecimal("103.9500000000"),
                position.getCurrentStopLoss()
        );
    }


    @Test
    void shouldTrailShortPositionAfterTarget() {

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

        /*
         * Target reached.
         *
         * 98 becomes the new SL.
         */
        position.targetReached();

        manager.update(
                position,
                new BigDecimal("95")
        );

        /*
         * 1% trailing:
         *
         * 95 + 1% = 95.95
         */
        assertEquals(
                new BigDecimal("95.9500000000"),
                position.getCurrentStopLoss()
        );
    }


    @Test
    void shouldNotMoveShortStopBackward() {

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

        /*
         * Price = 95
         * SL = 95.95
         */
        manager.update(
                position,
                new BigDecimal("95")
        );

        /*
         * Price moves against us to 97.
         *
         * New calculated SL would be 97.97.
         *
         * That would increase the SHORT stop.
         * We do NOT allow the stop to move backward.
         */
        manager.update(
                position,
                new BigDecimal("97")
        );

        assertEquals(
                new BigDecimal("95.9500000000"),
                position.getCurrentStopLoss()
        );
    }


    @Test
    void shouldContinueTrailingAsPriceMovesInProfit() {

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

        manager.update(
                position,
                new BigDecimal("105")
        );

        assertEquals(
                new BigDecimal("103.9500000000"),
                position.getCurrentStopLoss()
        );

        manager.update(
                position,
                new BigDecimal("110")
        );

        /*
         * 110 - 1% = 108.90
         */
        assertEquals(
                new BigDecimal("108.9000000000"),
                position.getCurrentStopLoss()
        );
    }
}