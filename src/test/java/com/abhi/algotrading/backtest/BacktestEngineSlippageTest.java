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

import static org.junit.jupiter.api.Assertions.assertEquals;

class BacktestEngineSlippageTest {

    @Test
    void shouldApplySlippageToEntryExitAndGrossPnl() {

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

        SlippageSimulator slippageSimulator =
                new SlippageSimulator(
                        new BigDecimal("0.10")
                );

        BacktestEngine backtestEngine =
                new BacktestEngine(
                        new MarketDataValidator(),
                        new OpeningRangeCalculator(),
                        tradingEngine,
                        slippageSimulator
                );

        BacktestResult result =
                backtestEngine.run(
                        createTradingDay()
                );

        assertEquals(
                1,
                result.getTotalTrades()
        );

        BacktestTrade trade =
                result.getTrades().getFirst();

        /*
         * Original theoretical entry:
         *
         * 25050
         *
         * LONG entry with 0.10% slippage:
         *
         * 25050 × 0.10% = 25.05
         *
         * Executed entry:
         *
         * 25050 + 25.05 = 25075.05
         */
        assertEquals(
                0,
                trade.entryPrice().compareTo(
                        new BigDecimal("25075.05")
                )
        );

        /*
         * Original theoretical exit:
         *
         * 25551
         *
         * LONG exit with 0.10% slippage:
         *
         * 25551 × 0.10% = 25.551
         *
         * Executed exit:
         *
         * 25551 - 25.551 = 25525.449
         */
        assertEquals(
                0,
                trade.exitPrice().compareTo(
                        new BigDecimal("25525.449")
                )
        );

        /*
         * Quantity remains determined by the
         * existing TradingEngine risk calculation.
         *
         * Quantity = 3
         */
        assertEquals(
                3,
                trade.quantity()
        );

        /*
         * Slippage-adjusted LONG P&L:
         *
         * Exit  = 25525.449
         * Entry = 25075.05
         *
         * Difference:
         *
         * 25525.449 - 25075.05
         * = 450.399
         *
         * Quantity = 3
         *
         * Gross P&L:
         *
         * 450.399 × 3
         * = 1351.197
         */
        assertEquals(
                0,
                trade.grossPnl().compareTo(
                        new BigDecimal("1351.197")
                )
        );

        assertEquals(
                PositionSide.LONG,
                trade.side()
        );

        assertEquals(
                "TRAILING_STOP",
                trade.exitReason()
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
         *
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
         * Trailing stop candidate:
         *
         * 25600 × (1 - 1%)
         * = 25344
         *
         * Current stop = 25551
         *
         * LONG stop cannot move backwards.
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
         * Theoretical exit = 25551.
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