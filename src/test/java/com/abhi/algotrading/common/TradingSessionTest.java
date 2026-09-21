package com.abhi.algotrading.common;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TradingSessionTest {

    @Test
    void shouldRecognizeMarketOpen() {

        assertTrue(
                TradingSession.isMarketOpen(
                        LocalTime.of(9, 15)
                )
        );
    }

    @Test
    void shouldRecognizeMarketDuringTradingHours() {

        assertTrue(
                TradingSession.isMarketOpen(
                        LocalTime.of(12, 30)
                )
        );
    }

    @Test
    void shouldRecognizeMarketClosedAt1530() {

        assertFalse(
                TradingSession.isMarketOpen(
                        LocalTime.of(15, 30)
                )
        );
    }


    // --------------------------------------------------
    // MORNING ENTRY
    // --------------------------------------------------

    @Test
    void shouldAllowMorningEntryAt0930() {

        assertTrue(
                TradingSession.isMorningEntryWindow(
                        LocalTime.of(9, 30)
                )
        );
    }

    @Test
    void shouldAllowMorningEntryAt1129() {

        assertTrue(
                TradingSession.isMorningEntryWindow(
                        LocalTime.of(11, 29)
                )
        );
    }

    @Test
    void shouldRejectMorningEntryAt1130() {

        assertFalse(
                TradingSession.isMorningEntryWindow(
                        LocalTime.of(11, 30)
                )
        );
    }


    // --------------------------------------------------
    // LATE SESSION ENTRY
    // --------------------------------------------------

    @Test
    void shouldAllowLateEntryAt1430() {

        assertTrue(
                TradingSession.isLateEntryWindow(
                        LocalTime.of(14, 30)
                )
        );
    }

    @Test
    void shouldAllowLateEntryAt1514() {

        assertTrue(
                TradingSession.isLateEntryWindow(
                        LocalTime.of(15, 14)
                )
        );
    }

    @Test
    void shouldRejectLateEntryAt1515() {

        assertFalse(
                TradingSession.isLateEntryWindow(
                        LocalTime.of(15, 15)
                )
        );
    }


    // --------------------------------------------------
    // NEW TRADE GATE
    // --------------------------------------------------

    @Test
    void shouldAllowNewTradeAt1000() {

        assertTrue(
                TradingSession.isNewTradeAllowed(
                        LocalTime.of(10, 0)
                )
        );
    }

    @Test
    void shouldAllowNewTradeAt1500() {

        assertTrue(
                TradingSession.isNewTradeAllowed(
                        LocalTime.of(15, 0)
                )
        );
    }

    @Test
    void shouldRejectNewTradeAt1520() {

        assertFalse(
                TradingSession.isNewTradeAllowed(
                        LocalTime.of(15, 20)
                )
        );
    }

    @Test
    void shouldRejectNewTradeAt1529() {

        assertFalse(
                TradingSession.isNewTradeAllowed(
                        LocalTime.of(15, 29)
                )
        );
    }


    // --------------------------------------------------
    // TRADE CUTOFF
    // --------------------------------------------------

    @Test
    void shouldNotReachCutoffAt1514() {

        assertFalse(
                TradingSession.isTradeCutoffReached(
                        LocalTime.of(15, 14)
                )
        );
    }

    @Test
    void shouldReachCutoffAt1515() {

        assertTrue(
                TradingSession.isTradeCutoffReached(
                        LocalTime.of(15, 15)
                )
        );
    }

    @Test
    void shouldRemainInCutoffAfter1515() {

        assertTrue(
                TradingSession.isTradeCutoffReached(
                        LocalTime.of(15, 25)
                )
        );
    }


    // --------------------------------------------------
    // END OF DAY
    // --------------------------------------------------

    @Test
    void shouldTriggerEndOfDayAt1530() {

        assertTrue(
                TradingSession.isEndOfDay(
                        LocalTime.of(15, 30)
                )
        );
    }

    @Test
    void shouldTriggerEndOfDayAfter1530() {

        assertTrue(
                TradingSession.isEndOfDay(
                        LocalTime.of(15, 31)
                )
        );
    }

    @Test
    void shouldNotTriggerEndOfDayAt1529() {

        assertFalse(
                TradingSession.isEndOfDay(
                        LocalTime.of(15, 29)
                )
        );
    }
}