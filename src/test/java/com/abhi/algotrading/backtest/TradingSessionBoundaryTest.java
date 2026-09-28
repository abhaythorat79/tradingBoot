package com.abhi.algotrading.backtest;

import com.abhi.algotrading.common.TradingSession;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class TradingSessionBoundaryTest {

    @Test
    void marketShouldBeOpenAt0915() {

        assertTrue(
                TradingSession.isMarketOpen(
                        LocalTime.of(9, 15)
                )
        );
    }

    @Test
    void marketShouldBeOpenJustBefore1530() {

        assertTrue(
                TradingSession.isMarketOpen(
                        LocalTime.of(15, 29, 59)
                )
        );
    }

    @Test
    void marketShouldBeClosedAt1530() {

        assertFalse(
                TradingSession.isMarketOpen(
                        LocalTime.of(15, 30)
                )
        );
    }

    @Test
    void morningEntryShouldBeAllowedAt0930() {

        assertTrue(
                TradingSession.isMorningEntryWindow(
                        LocalTime.of(9, 30)
                )
        );
    }

    @Test
    void morningEntryShouldBeAllowedJustBefore1130() {

        assertTrue(
                TradingSession.isMorningEntryWindow(
                        LocalTime.of(11, 29, 59)
                )
        );
    }

    @Test
    void morningEntryShouldNotBeAllowedAt1130() {

        assertFalse(
                TradingSession.isMorningEntryWindow(
                        LocalTime.of(11, 30)
                )
        );
    }

    @Test
    void lateEntryShouldNotBeAllowedBefore1430() {

        assertFalse(
                TradingSession.isLateEntryWindow(
                        LocalTime.of(14, 29, 59)
                )
        );
    }

    @Test
    void lateEntryShouldBeAllowedAt1430() {

        assertTrue(
                TradingSession.isLateEntryWindow(
                        LocalTime.of(14, 30)
                )
        );
    }

    @Test
    void lateEntryShouldBeAllowedJustBefore1515() {

        assertTrue(
                TradingSession.isLateEntryWindow(
                        LocalTime.of(15, 14, 59)
                )
        );
    }

    @Test
    void newTradeShouldNotBeAllowedAt1515() {

        assertFalse(
                TradingSession.isNewTradeAllowed(
                        LocalTime.of(15, 15)
                )
        );
    }

    @Test
    void newTradeShouldNotBeAllowedAfter1515() {

        assertFalse(
                TradingSession.isNewTradeAllowed(
                        LocalTime.of(15, 20)
                )
        );
    }

    @Test
    void tradeCutoffShouldNotBeReachedBefore1515() {

        assertFalse(
                TradingSession.isTradeCutoffReached(
                        LocalTime.of(15, 14, 59)
                )
        );
    }

    @Test
    void tradeCutoffShouldBeReachedAt1515() {

        assertTrue(
                TradingSession.isTradeCutoffReached(
                        LocalTime.of(15, 15)
                )
        );
    }

    @Test
    void endOfDayShouldNotBeReachedBefore1530() {

        assertFalse(
                TradingSession.isEndOfDay(
                        LocalTime.of(15, 29, 59)
                )
        );
    }

    @Test
    void endOfDayShouldBeReachedAt1530() {

        assertTrue(
                TradingSession.isEndOfDay(
                        LocalTime.of(15, 30)
                )
        );
    }

    @Test
    void newTradeShouldNotBeAllowedDuringOpeningRange() {

        assertFalse(
                TradingSession.isNewTradeAllowed(
                        LocalTime.of(9, 29, 59)
                )
        );
    }

    @Test
    void newTradeShouldBeAllowedAt0930() {

        assertTrue(
                TradingSession.isNewTradeAllowed(
                        LocalTime.of(9, 30)
                )
        );
    }

    @Test
    void newTradeShouldBeAllowedAt1430() {

        assertTrue(
                TradingSession.isNewTradeAllowed(
                        LocalTime.of(14, 30)
                )
        );
    }

    @Test
    void newTradeShouldNotBeAllowedAt1130ButLateWindowIsNotStarted() {

        assertFalse(
                TradingSession.isNewTradeAllowed(
                        LocalTime.of(11, 30)
                )
        );
    }
}