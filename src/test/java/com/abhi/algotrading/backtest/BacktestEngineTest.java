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
}