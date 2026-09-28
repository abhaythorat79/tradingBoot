package com.abhi.algotrading.backtest;

import com.abhi.algotrading.confirmation.CandleConfirmation;
import com.abhi.algotrading.execution.TradeAction;
import com.abhi.algotrading.execution.TradeResult;
import com.abhi.algotrading.execution.TradingEngine;
import com.abhi.algotrading.marketdata.Candle;
import com.abhi.algotrading.risk.CapitalUtilizationCalculator;
import com.abhi.algotrading.risk.CapitalUtilizationConfig;
import com.abhi.algotrading.risk.PositionSizer;
import com.abhi.algotrading.risk.RiskConfig;
import com.abhi.algotrading.risk.RiskManager;
import com.abhi.algotrading.risk.TrailingStopManager;
import com.abhi.algotrading.strategy.BreakoutDetector;
import com.abhi.algotrading.strategy.OpeningRange;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SameCandleExitPriorityTest {

    @Test
    void shouldGiveStopLossPriorityWhenLongCandleHitsBothStopAndTarget() {

        TradingEngine tradingEngine =
                createTradingEngine();

        OpeningRange openingRange =
                createOpeningRange();

        /*
         * Entry:
         * 25,050
         *
         * Initial stop:
         * 25,050 - 1% = 24,799.50
         *
         * Initial target:
         * 25,050 + 2% = 25,551
         */
        Candle entryCandle =
                candle(
                        "2026-01-05T09:31:00",
                        "NIFTY",
                        "25000",
                        "25100",
                        "24990",
                        "25050"
                );

        TradeResult entryResult =
                tradingEngine.processCandle(
                        openingRange,
                        entryCandle
                );

        assertEquals(
                TradeAction.OPEN_LONG,
                entryResult.action()
        );

        assertNotNull(
                tradingEngine.getOpenPosition()
        );

        /*
         * This single candle touches BOTH:
         *
         * High = 25,600
         * -> target 25,551 touched
         *
         * Low = 24,700
         * -> stop 24,799.50 touched
         *
         * OHLC data cannot tell us which happened
         * first.
         *
         * Current engine policy is:
         *
         * STOP LOSS first.
         */
        Candle ambiguousCandle =
                candle(
                        "2026-01-05T09:32:00",
                        "NIFTY",
                        "25050",
                        "25600",
                        "24700",
                        "25300"
                );

        TradeResult result =
                tradingEngine.processCandle(
                        openingRange,
                        ambiguousCandle
                );

        assertEquals(
                TradeAction.STOP_LOSS_EXIT,
                result.action()
        );

        assertNotNull(
                result.position()
        );

        assertFalse(
                result.position().isOpen()
        );

        assertNull(
                tradingEngine.getOpenPosition()
        );

        /*
         * Because the stop is selected first,
         * the exit price must equal the initial SL.
         */
        assertEquals(
                new BigDecimal("24799.50"),
                result.position().getExitPrice()
        );
    }

    @Test
    void shouldNotReportTargetReachedWhenSameCandleAlsoHitsStop() {

        TradingEngine tradingEngine =
                createTradingEngine();

        OpeningRange openingRange =
                createOpeningRange();

        Candle entryCandle =
                candle(
                        "2026-01-05T09:31:00",
                        "NIFTY",
                        "25000",
                        "25100",
                        "24990",
                        "25050"
                );

        TradeResult entryResult =
                tradingEngine.processCandle(
                        openingRange,
                        entryCandle
                );

        assertEquals(
                TradeAction.OPEN_LONG,
                entryResult.action()
        );

        Candle ambiguousCandle =
                candle(
                        "2026-01-05T09:32:00",
                        "NIFTY",
                        "25050",
                        "25600",
                        "24700",
                        "25300"
                );

        TradeResult result =
                tradingEngine.processCandle(
                        openingRange,
                        ambiguousCandle
                );

        /*
         * TargetReached must NOT be returned because
         * the current policy gives SL priority.
         */
        assertNotEquals(
                TradeAction.TARGET_REACHED,
                result.action()
        );

        assertEquals(
                TradeAction.STOP_LOSS_EXIT,
                result.action()
        );
    }

    private TradingEngine createTradingEngine() {

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

        CapitalUtilizationCalculator
                utilizationCalculator =
                new CapitalUtilizationCalculator(
                        new CapitalUtilizationConfig(
                                new BigDecimal("100")
                        )
                );

        return new TradingEngine(
                new BreakoutDetector(),
                new CandleConfirmation(),
                new PositionSizer(),
                riskManager,
                new TrailingStopManager(
                        riskConfig.trailingStopPercent()
                ),
                riskConfig,
                utilizationCalculator
        );
    }

    private OpeningRange createOpeningRange() {

        return new OpeningRange(
                LocalDateTime.of(
                        2026, 1, 5, 9, 15
                ),
                LocalDateTime.of(
                        2026, 1, 5, 9, 30
                ),
                new BigDecimal("25000"),
                new BigDecimal("24500")
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