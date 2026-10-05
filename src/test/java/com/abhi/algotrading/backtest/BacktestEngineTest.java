package com.abhi.algotrading.backtest;

import com.abhi.algotrading.confirmation.CandleConfirmation;
import com.abhi.algotrading.execution.TradingEngine;
import com.abhi.algotrading.marketdata.Candle;
import com.abhi.algotrading.marketdata.MarketDataValidator;
import com.abhi.algotrading.portfolio.PositionSide;
import com.abhi.algotrading.risk.PositionSizer;
import com.abhi.algotrading.risk.RiskConfig;
import com.abhi.algotrading.risk.RiskManager;
import com.abhi.algotrading.risk.TrailingStopManager;
import com.abhi.algotrading.strategy.BreakoutDetector;
import com.abhi.algotrading.strategy.OpeningRangeCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BacktestEngineTest {

    @Test
    void shouldRunBacktestForOneTradingDay() {

        RiskConfig riskConfig =
                new RiskConfig(
                        new BigDecimal("100000"),
                        new BigDecimal("1"),
                        new BigDecimal("5000"),
                        3,
                        3,
                        new BigDecimal("1"),
                        new BigDecimal("2"),
                        new BigDecimal("1")
                );

        RiskManager riskManager =
                new RiskManager(riskConfig);

        TradingEngine tradingEngine =
                new TradingEngine(
                        new BreakoutDetector(),
                        new CandleConfirmation(),
                        new PositionSizer(),
                        riskManager,
                        new TrailingStopManager(
                                new BigDecimal("1")
                        ),
                        riskConfig
                );

        BacktestEngine backtestEngine =
                new BacktestEngine(
                        new MarketDataValidator(),
                        new OpeningRangeCalculator(),
                        tradingEngine
                );

        List<Candle> candles =
                createTradingDay();

        BacktestResult result =
                backtestEngine.run(candles);

        assertEquals(
                1,
                result.getTradingDays()
        );

        assertEquals(
                1,
                result.getTotalTrades()
        );

        assertEquals(
                1,
                result.getWinningTrades()
        );

        assertEquals(
                0,
                result.getLosingTrades()
        );

        assertEquals(
                0,
                result.getGrossPnl()
                        .compareTo(
                                new BigDecimal("1503")
                        )
        );

        BacktestTrade trade =
                result.getTrades().getFirst();

        assertEquals(
                "NIFTY",
                trade.symbol()
        );

        assertEquals(
                PositionSide.LONG,
                trade.side()
        );

        /*
         * Risk calculation:
         *
         * Capital = 100000
         * Risk = 1% = 1000
         *
         * Entry = 25050
         * Stop Loss = 24799.50
         *
         * Risk per unit = 250.50
         *
         * Quantity = 1000 / 250.50
         *           = 3.99
         *
         * Rounded DOWN = 3
         */
        assertEquals(
                3,
                trade.quantity()
        );

        assertEquals(
                "TRAILING_STOP",
                trade.exitReason()
        );

        /*
         * Entry = 25050
         *
         * Initial target:
         * 25050 + 2% = 25551
         *
         * Target is reached at 09:31.
         *
         * Target becomes stop loss:
         * 25551
         *
         * Trailing stop cannot move the stop
         * backwards because the position is LONG.
         *
         * Therefore exit price = 25551.
         *
         * Profit per unit = 25551 - 25050
         *                 = 501
         *
         * Gross P&L = 501 × 3
         *           = 1503
         */
        assertEquals(
                0,
                trade.entryPrice()
                        .compareTo(
                                new BigDecimal("25050")
                        )
        );

        assertEquals(
                0,
                trade.exitPrice()
                        .compareTo(
                                new BigDecimal("25551")
                        )
        );

        assertEquals(
                0,
                trade.grossPnl()
                        .compareTo(
                                new BigDecimal("1503")
                        )
        );
    }

    private List<Candle> createTradingDay() {

        List<Candle> candles =
                new ArrayList<>();

        /*
         * Opening Range
         *
         * 09:15 - 09:29
         *
         * High = 25020
         * Low  = 24990
         */

        candles.add(
                candle(
                        "09:15:00",
                        "25000",
                        "25010",
                        "24990",
                        "25000",
                        100000
                )
        );

        candles.add(
                candle(
                        "09:16:00",
                        "25000",
                        "25020",
                        "24995",
                        "25010",
                        100000
                )
        );

        candles.add(
                candle(
                        "09:17:00",
                        "25010",
                        "25015",
                        "24995",
                        "25005",
                        100000
                )
        );

        /*
         * Remaining opening-range candles.
         *
         * 09:18 - 09:29
         */

        for (int minute = 18;
             minute <= 29;
             minute++) {

            candles.add(
                    candle(
                            String.format(
                                    "09:%02d:00",
                                    minute
                            ),
                            "25005",
                            "25015",
                            "24995",
                            "25005",
                            100000
                    )
            );
        }

        /*
         * 09:30 BREAKOUT
         *
         * Opening Range High = 25020
         * Candle High = 25060
         * Candle Close = 25050
         *
         * UP breakout + candle-close confirmation.
         */

        candles.add(
                candle(
                        "09:30:00",
                        "25050",
                        "25060",
                        "25010",
                        "25050",
                        150000
                )
        );

        /*
         * 09:31
         *
         * Entry = 25050
         *
         * Initial target:
         * 25050 + 2% = 25551
         *
         * High reaches 25600.
         *
         * Target is activated.
         */

        candles.add(
                candle(
                        "09:31:00",
                        "25050",
                        "25600",
                        "25040",
                        "25580",
                        180000
                )
        );

        /*
         * 09:32
         *
         * Target has already been reached.
         *
         * Trailing stop calculation:
         *
         * Close = 25600
         * 1% = 256
         * Candidate trailing stop = 25344
         *
         * Current stop = 25551
         *
         * Because this is LONG, the stop cannot move down.
         *
         * Therefore current stop remains 25551.
         */

        candles.add(
                candle(
                        "09:32:00",
                        "25580",
                        "25600",
                        "25570",
                        "25600",
                        160000
                )
        );

        /*
         * 09:33
         *
         * Current stop = 25551
         *
         * Low = 25300
         *
         * Stop is hit.
         *
         * Exit price = 25551.
         */

        candles.add(
                candle(
                        "09:33:00",
                        "25600",
                        "25610",
                        "25300",
                        "25300",
                        150000
                )
        );

        /*
         * 15:30 EOD candle.
         *
         * Position should already be closed.
         */

        candles.add(
                candle(
                        "15:30:00",
                        "25300",
                        "25310",
                        "25290",
                        "25300",
                        100000
                )
        );

        return candles;
    }

    private Candle candle(
            String time,
            String open,
            String high,
            String low,
            String close,
            long volume) {

        return new Candle(
                LocalDateTime.parse(
                        "2026-09-21T" + time
                ),
                "NIFTY",
                new BigDecimal(open),
                new BigDecimal(high),
                new BigDecimal(low),
                new BigDecimal(close),
                volume
        );
    }

    @Test
    void shouldNotUseFutureCandleToCreateEarlierTrade() {

        RiskConfig riskConfig =
                new RiskConfig(
                        new BigDecimal("100000"),
                        new BigDecimal("1"),
                        new BigDecimal("5000"),
                        3,
                        3,
                        new BigDecimal("1"),
                        new BigDecimal("2"),
                        new BigDecimal("1")
                );

        RiskManager riskManager =
                new RiskManager(riskConfig);

        TradingEngine tradingEngine =
                new TradingEngine(
                        new BreakoutDetector(),
                        new CandleConfirmation(),
                        new PositionSizer(),
                        riskManager,
                        new TrailingStopManager(
                                new BigDecimal("1")
                        ),
                        riskConfig
                );

        BacktestEngine backtestEngine =
                new BacktestEngine(
                        new MarketDataValidator(),
                        new OpeningRangeCalculator(),
                        tradingEngine
                );

        List<Candle> candles =
                List.of(

                        /*
                         * Opening Range
                         */
                        candle(
                                "09:15:00",
                                "100",
                                "105",
                                "99",
                                "103",
                                100000
                        ),

                        candle(
                                "09:20:00",
                                "103",
                                "108",
                                "102",
                                "107",
                                100000
                        ),

                        candle(
                                "09:25:00",
                                "107",
                                "110",
                                "106",
                                "109",
                                100000
                        ),

                        /*
                         * 09:30
                         *
                         * No breakout.
                         * Therefore there must be no trade here.
                         */
                        candle(
                                "09:30:00",
                                "109",
                                "109",
                                "108",
                                "108.50",
                                100000
                        ),

                        /*
                         * 10:00
                         *
                         * Future breakout.
                         *
                         * This candle is allowed to create a trade
                         * at 10:00, but it must NOT retroactively
                         * create a trade at 09:30.
                         */
                        candle(
                                "10:00:00",
                                "109",
                                "120",
                                "108",
                                "115",
                                150000
                        ),

                        /*
                         * 15:30
                         */
                        candle(
                                "15:30:00",
                                "115",
                                "116",
                                "114",
                                "115",
                                100000
                        )
                );

        BacktestResult result =
                backtestEngine.run(candles);

        /*
         * Exactly one trade may exist.
         *
         * It must originate from the future 10:00
         * breakout candle, not from 09:30.
         */
        assertEquals(
                1,
                result.getTotalTrades()
        );

        BacktestTrade trade =
                result.getTrades().getFirst();

        assertEquals(
                LocalDateTime.parse(
                        "2026-09-21T10:00:00"
                ),
                trade.entryTime()
        );
    }

    @Test
    void shouldKeepTradingDaysIsolated() {

        RiskConfig riskConfig =
                new RiskConfig(
                        new BigDecimal("100000"),
                        new BigDecimal("1"),
                        new BigDecimal("5000"),
                        3,
                        3,
                        new BigDecimal("1"),
                        new BigDecimal("2"),
                        new BigDecimal("1")
                );

        /*
         * -------------------------
         * BACKTEST WITH DAY 1 ONLY
         * -------------------------
         */

        RiskManager day1OnlyRiskManager =
                new RiskManager(riskConfig);

        TradingEngine day1OnlyTradingEngine =
                new TradingEngine(
                        new BreakoutDetector(),
                        new CandleConfirmation(),
                        new PositionSizer(),
                        day1OnlyRiskManager,
                        new TrailingStopManager(
                                new BigDecimal("1")
                        ),
                        riskConfig
                );

        BacktestEngine day1OnlyBacktest =
                new BacktestEngine(
                        new MarketDataValidator(),
                        new OpeningRangeCalculator(),
                        day1OnlyTradingEngine
                );

        List<Candle> day1OnlyCandles =
                createDay1ForIsolationTest();

        BacktestResult day1OnlyResult =
                day1OnlyBacktest.run(
                        day1OnlyCandles
                );

        /*
         * Day 1 must produce exactly one
         * trading day and one trade.
         */

        assertEquals(
                1,
                day1OnlyResult.getTradingDays()
        );

        assertEquals(
                1,
                day1OnlyResult.getTotalTrades()
        );

        BacktestTrade day1OnlyTrade =
                day1OnlyResult.getTrades().getFirst();

        /*
         * -------------------------
         * BACKTEST WITH DAY 1 + DAY 2
         * -------------------------
         */

        RiskManager twoDayRiskManager =
                new RiskManager(riskConfig);

        TradingEngine twoDayTradingEngine =
                new TradingEngine(
                        new BreakoutDetector(),
                        new CandleConfirmation(),
                        new PositionSizer(),
                        twoDayRiskManager,
                        new TrailingStopManager(
                                new BigDecimal("1")
                        ),
                        riskConfig
                );

        BacktestEngine twoDayBacktest =
                new BacktestEngine(
                        new MarketDataValidator(),
                        new OpeningRangeCalculator(),
                        twoDayTradingEngine
                );

        List<Candle> twoDayCandles =
                new ArrayList<>(
                        day1OnlyCandles
                );

        twoDayCandles.addAll(
                createDay2ForIsolationTest()
        );

        BacktestResult twoDayResult =
                twoDayBacktest.run(
                        twoDayCandles
                );

        /*
         * There must now be exactly two
         * trading days.
         */

        assertEquals(
                2,
                twoDayResult.getTradingDays()
        );

        /*
         * Each day should produce one trade.
         */

        assertEquals(
                2,
                twoDayResult.getTotalTrades()
        );

        /*
         * Find the Day 1 trade from the
         * combined two-day backtest.
         */

        BacktestTrade combinedDay1Trade =
                twoDayResult.getTrades()
                        .stream()
                        .filter(trade ->
                                trade.entryTime()
                                        .toLocalDate()
                                        .equals(
                                                LocalDateTime.parse(
                                                        "2026-09-21T09:30:00"
                                                ).toLocalDate()
                                        )
                        )
                        .findFirst()
                        .orElseThrow();

        /*
         * Day 1 entry must remain exactly
         * the same after adding Day 2.
         */

        assertEquals(
                day1OnlyTrade.entryTime(),
                combinedDay1Trade.entryTime()
        );

        assertEquals(
                day1OnlyTrade.entryPrice(),
                combinedDay1Trade.entryPrice()
        );

        assertEquals(
                day1OnlyTrade.exitPrice(),
                combinedDay1Trade.exitPrice()
        );

        assertEquals(
                day1OnlyTrade.quantity(),
                combinedDay1Trade.quantity()
        );

        assertEquals(
                day1OnlyTrade.grossPnl(),
                combinedDay1Trade.grossPnl()
        );

        assertEquals(
                day1OnlyTrade.exitReason(),
                combinedDay1Trade.exitReason()
        );
    }

    @Test
    void shouldRejectOutOfOrderCandles() {

        RiskConfig riskConfig =
                new RiskConfig(
                        new BigDecimal("100000"),
                        new BigDecimal("1"),
                        new BigDecimal("5000"),
                        3,
                        3,
                        new BigDecimal("1"),
                        new BigDecimal("2"),
                        new BigDecimal("1")
                );

        RiskManager riskManager =
                new RiskManager(riskConfig);

        TradingEngine tradingEngine =
                new TradingEngine(
                        new BreakoutDetector(),
                        new CandleConfirmation(),
                        new PositionSizer(),
                        riskManager,
                        new TrailingStopManager(
                                new BigDecimal("1")
                        ),
                        riskConfig
                );

        BacktestEngine backtestEngine =
                new BacktestEngine(
                        new MarketDataValidator(),
                        new OpeningRangeCalculator(),
                        tradingEngine
                );

        List<Candle> candles =
                new ArrayList<>(
                        createTradingDay()
                );

        /*
         * Intentionally swap two candles.
         *
         * This creates invalid chronological
         * market data.
         */
        Collections.swap(
                candles,
                0,
                1
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> backtestEngine.run(candles)
                );

        assertTrue(
                exception.getMessage()
                        .contains(
                                "Candles must be in chronological order"
                        )
        );
    }

    private List<Candle> createDay1ForIsolationTest() {

        List<Candle> candles =
                new ArrayList<>();

        /*
         * Day 1
         *
         * Date = 2026-09-21
         *
         * Opening Range:
         *
         * High = 110
         * Low  = 99
         */

        candles.add(
                candle(
                        "2026-09-21",
                        "09:15:00",
                        "100",
                        "105",
                        "99",
                        "103",
                        100000
                )
        );

        candles.add(
                candle(
                        "2026-09-21",
                        "09:20:00",
                        "103",
                        "108",
                        "102",
                        "107",
                        100000
                )
        );

        candles.add(
                candle(
                        "2026-09-21",
                        "09:25:00",
                        "107",
                        "110",
                        "106",
                        "109",
                        100000
                )
        );

        /*
         * 09:30 breakout.
         */

        candles.add(
                candle(
                        "2026-09-21",
                        "09:30:00",
                        "109",
                        "120",
                        "108",
                        "115",
                        150000
                )
        );

        /*
         * 15:30 EOD.
         *
         * The open position must be closed
         * by the end of the trading day.
         */

        candles.add(
                candle(
                        "2026-09-21",
                        "15:30:00",
                        "115",
                        "116",
                        "114",
                        "115",
                        100000
                )
        );

        return candles;
    }

    private List<Candle> createDay2ForIsolationTest() {

        List<Candle> candles =
                new ArrayList<>();

        /*
         * Day 2
         *
         * IMPORTANT:
         *
         * This is a different trading date.
         *
         * Date = 2026-09-22
         */

        candles.add(
                candle(
                        "2026-09-22",
                        "09:15:00",
                        "200",
                        "205",
                        "198",
                        "203",
                        100000
                )
        );

        candles.add(
                candle(
                        "2026-09-22",
                        "09:20:00",
                        "203",
                        "208",
                        "202",
                        "207",
                        100000
                )
        );

        candles.add(
                candle(
                        "2026-09-22",
                        "09:25:00",
                        "207",
                        "210",
                        "206",
                        "209",
                        100000
                )
        );

        /*
         * 09:30 breakout.
         */

        candles.add(
                candle(
                        "2026-09-22",
                        "09:30:00",
                        "209",
                        "220",
                        "208",
                        "215",
                        150000
                )
        );

        /*
         * 15:30 EOD.
         */

        candles.add(
                candle(
                        "2026-09-22",
                        "15:30:00",
                        "215",
                        "216",
                        "214",
                        "215",
                        100000
                )
        );

        return candles;
    }

    private Candle candle(
            String date,
            String time,
            String open,
            String high,
            String low,
            String close,
            long volume) {

        return new Candle(
                LocalDateTime.parse(
                        date + "T" + time
                ),
                "NIFTY",
                new BigDecimal(open),
                new BigDecimal(high),
                new BigDecimal(low),
                new BigDecimal(close),
                volume
        );
    }

    @Test
    void shouldRejectDuplicateTimestampCandles() {

        RiskConfig riskConfig =
                new RiskConfig(
                        new BigDecimal("100000"),
                        new BigDecimal("1"),
                        new BigDecimal("5000"),
                        3,
                        3,
                        new BigDecimal("1"),
                        new BigDecimal("2"),
                        new BigDecimal("1")
                );

        RiskManager riskManager =
                new RiskManager(riskConfig);

        TradingEngine tradingEngine =
                new TradingEngine(
                        new BreakoutDetector(),
                        new CandleConfirmation(),
                        new PositionSizer(),
                        riskManager,
                        new TrailingStopManager(
                                new BigDecimal("1")
                        ),
                        riskConfig
                );

        BacktestEngine backtestEngine =
                new BacktestEngine(
                        new MarketDataValidator(),
                        new OpeningRangeCalculator(),
                        tradingEngine
                );

        List<Candle> candles =
                new ArrayList<>();

        /*
         * First candle.
         */
        candles.add(
                candle(
                        "09:15:00",
                        "25000",
                        "25020",
                        "24990",
                        "25010",
                        100000
                )
        );

        /*
         * Second candle has the EXACT
         * same timestamp as the first candle.
         *
         * This must be rejected.
         */
        candles.add(
                candle(
                        "09:15:00",
                        "25010",
                        "25030",
                        "25000",
                        "25025",
                        120000
                )
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> backtestEngine.run(candles)
                );

        assertTrue(
                exception.getMessage()
                        .contains("Duplicate timestamp")
        );
    }

}