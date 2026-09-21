package com.abhi.algotrading.common;

import java.time.LocalTime;

public final class TradingSession {

    private TradingSession() {
    }

    /*
     * Complete market session
     */
    public static final LocalTime MARKET_OPEN =
            LocalTime.of(9, 15);

    public static final LocalTime MARKET_CLOSE =
            LocalTime.of(15, 30);


    /*
     * Opening Range
     *
     * 09:15 -> 09:30
     */
    public static final LocalTime OPENING_RANGE_END =
            LocalTime.of(9, 30);


    /*
     * Morning ORB entry
     *
     * 09:30 -> 11:30
     */
    public static final LocalTime MORNING_ENTRY_START =
            LocalTime.of(9, 30);

    public static final LocalTime MORNING_ENTRY_END =
            LocalTime.of(11, 30);


    /*
     * Late-session entry
     *
     * 14:30 -> 15:15
     *
     * No new trades after 15:15.
     */
    public static final LocalTime LATE_ENTRY_START =
            LocalTime.of(14, 30);

    public static final LocalTime NEW_TRADE_CUTOFF =
            LocalTime.of(15, 15);


    /*
     * Check whether the market is inside
     * the complete trading session.
     *
     * 09:15 inclusive
     * 15:30 exclusive
     */
    public static boolean isMarketOpen(
            LocalTime time) {

        return !time.isBefore(MARKET_OPEN)
                && time.isBefore(MARKET_CLOSE);
    }


    /*
     * Morning ORB entry window.
     *
     * 09:30 inclusive
     * 11:30 exclusive
     */
    public static boolean isMorningEntryWindow(
            LocalTime time) {

        return !time.isBefore(MORNING_ENTRY_START)
                && time.isBefore(MORNING_ENTRY_END);
    }


    /*
     * Late-session entry window.
     *
     * 14:30 inclusive
     * 15:15 exclusive
     */
    public static boolean isLateEntryWindow(
            LocalTime time) {

        return !time.isBefore(LATE_ENTRY_START)
                && time.isBefore(NEW_TRADE_CUTOFF);
    }


    /*
     * Determines whether a NEW trade is allowed.
     *
     * This is intentionally separate from
     * position management.
     */
    public static boolean isNewTradeAllowed(
            LocalTime time) {

        return isMorningEntryWindow(time)
                || isLateEntryWindow(time);
    }


    /*
     * After 15:15:
     *
     * - No new trades
     * - Existing positions can still be managed
     * - All positions must be closed at 15:30
     */
    public static boolean isTradeCutoffReached(
            LocalTime time) {

        return !time.isBefore(NEW_TRADE_CUTOFF);
    }


    /*
     * Hard end-of-day exit.
     *
     * At 15:30 all positions must be closed.
     */
    public static boolean isEndOfDay(
            LocalTime time) {

        return !time.isBefore(MARKET_CLOSE);
    }
}