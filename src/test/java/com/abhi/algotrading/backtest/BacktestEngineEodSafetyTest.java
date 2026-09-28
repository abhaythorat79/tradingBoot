package com.abhi.algotrading.backtest;

import com.abhi.algotrading.confirmation.CandleConfirmation;
import com.abhi.algotrading.execution.TradeAction;
import com.abhi.algotrading.execution.TradingEngine;
import com.abhi.algotrading.marketdata.Candle;
import com.abhi.algotrading.marketdata.MarketDataValidator;
import com.abhi.algotrading.risk.CapitalUtilizationCalculator;
import com.abhi.algotrading.risk.CapitalUtilizationConfig;
import com.abhi.algotrading.risk.PositionSizer;
import com.abhi.algotrading.risk.RiskConfig;
import com.abhi.algotrading.risk.RiskManager;
import com.abhi.algotrading.risk.TrailingStopManager;
import com.abhi.algotrading.strategy.BreakoutDetector;
import com.abhi.algotrading.strategy.OpeningRangeCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BacktestEngineEodSafetyTest {

    @Test
    void shouldCloseRemainingPositionAtEndOfDay() {

        RiskConfig riskConfig =
                new RiskConfig(
                        new BigDecimal("100000"),
                        new BigDecimal("1"),
                        new BigDecimal("10000"),
                        10,
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
                                riskConfig.trailingStopPercent()
                        ),
                        riskConfig,
                        new CapitalUtilizationCalculator(
                                new CapitalUtilizationConfig(
                                        new BigDecimal("100")
                                )
                        )
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
                         * Opening range candle.
                         */
                        candle(
                                "2026-01-05T09:15:00",
                                "NIFTY",
                                "25000",
                                "25050",
                                "24950",
                                "25000"
                        ),

                        /*
                         * Second opening range candle.
                         */
                        candle(
                                "2026-01-05T09:20:00",
                                "NIFTY",
                                "25000",
                                "25020",
                                "24980",
                                "25000"
                        ),

                        /*
                         * Confirmed LONG breakout.
                         *
                         * Opening range high = 25050
                         * Candle high = 25100
                         * Candle close = 25060
                         */
                        candle(
                                "2026-01-05T09:31:00",
                                "NIFTY",
                                "25000",
                                "25100",
                                "24990",
                                "25060"
                        ),

                        /*
                         * Last available candle is 15:29.
                         *
                         * There is intentionally NO 15:30 candle.
                         *
                         * BacktestEngine must therefore use
                         * its fallback EOD close.
                         */
                        candle(
                                "2026-01-05T15:29:00",
                                "NIFTY",
                                "25060",
                                "25150",
                                "25020",
                                "25100"
                        )
                );

        BacktestResult result =
                backtestEngine.run(candles);

        /*
         * One trading day should be processed.
         */
        assertEquals(
                1,
                result.getTradingDays()
        );

        /*
         * One completed trade should exist.
         */
        assertEquals(
                1,
                result.getTrades().size()
        );

        BacktestTrade trade =
                result.getTrades().getFirst();

        /*
         * BacktestTrade stores the normalized exit reason.
         *
         * END_OF_DAY_EXIT is mapped by BacktestEngine
         * to the string "END_OF_DAY".
         */
        assertEquals(
                "END_OF_DAY",
                trade.exitReason()
        );

        /*
         * Last available candle close = 25100.
         *
         * Therefore fallback EOD exit price must be 25100.
         */
        assertEquals(
                new BigDecimal("25100"),
                trade.exitPrice()
        );

        /*
         * Entry = 25060
         * Exit  = 25100
         * Difference = 40
         *
         * Quantity:
         * floor(1000 / 250.60) = 3
         *
         * Gross P&L:
         * 40 * 3 = 120
         */
        assertEquals(
                new BigDecimal("120"),
                trade.grossPnl()
        );

        /*
         * The fallback EOD close must not leave
         * an open position in TradingEngine.
         */
        assertNull(
                tradingEngine.getOpenPosition()
        );

        /*
         * Important safety assertion:
         *
         * The fallback EOD close should also record
         * realized P&L in RiskManager.
         *
         * Current BacktestEngine directly closes the
         * Position in its fallback path, so this assertion
         * is expected to expose whether RiskManager received
         * the realized P&L.
         */
        assertEquals(
                new BigDecimal("120"),
                riskManager.getDailyPnl()
        );
    }

    private Candle candle(
            String timestamp,
            String symbol,
            String open,
            String high,
            String low,
            String close) {

        return new Candle(
                LocalDateTime.parse(timestamp),
                symbol,
                new BigDecimal(open),
                new BigDecimal(high),
                new BigDecimal(low),
                new BigDecimal(close),
                100000
        );
    }
}