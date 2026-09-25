package com.abhi.algotrading.backtest;

import com.abhi.algotrading.confirmation.CandleConfirmation;
import com.abhi.algotrading.execution.TradeAction;
import com.abhi.algotrading.execution.TradeResult;
import com.abhi.algotrading.execution.TradingEngine;
import com.abhi.algotrading.marketdata.Candle;
import com.abhi.algotrading.marketdata.MarketDataValidator;
import com.abhi.algotrading.risk.PositionSizer;
import com.abhi.algotrading.risk.RiskConfig;
import com.abhi.algotrading.risk.RiskManager;
import com.abhi.algotrading.risk.TrailingStopManager;
import com.abhi.algotrading.strategy.BreakoutDetector;
import com.abhi.algotrading.strategy.OpeningRange;
import com.abhi.algotrading.strategy.OpeningRangeCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ConsecutiveLossLimitIntegrationTest {

    @Test
    void shouldBlockNewTradeAfterMaximumConsecutiveLosses() {

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
                createTradingEngine(
                        riskConfig,
                        riskManager
                );

        OpeningRange openingRange =
                new OpeningRange(
                        LocalDateTime.of(
                                2026, 1, 5, 9, 15
                        ),
                        LocalDateTime.of(
                                2026, 1, 5, 9, 30
                        ),
                        new BigDecimal("25000"),
                        new BigDecimal("24500")
                );

        /*
         * Entry:
         * 25,030
         *
         * Initial SL:
         * 25,030 - 1% = 24,779.70
         *
         * Risk per unit:
         * 250.30
         *
         * Risk amount:
         * 1% of 100,000 = 1,000
         *
         * Quantity:
         * floor(1000 / 250.30) = 3
         *
         * Loss:
         * 250.30 * 3 = 750.90
         */

        Candle firstEntry =
                candle(
                        "2026-01-05T09:31:00",
                        "NIFTY",
                        "25000",
                        "25050",
                        "24990",
                        "25030"
                );

        TradeResult firstOpen =
                tradingEngine.processCandle(
                        openingRange,
                        firstEntry
                );

        assertEquals(
                TradeAction.OPEN_LONG,
                firstOpen.action()
        );

        assertNotNull(
                firstOpen.position()
        );

        Candle firstStop =
                candle(
                        "2026-01-05T09:32:00",
                        "NIFTY",
                        "25030",
                        "25040",
                        "24700",
                        "24800"
                );

        TradeResult firstExit =
                tradingEngine.processCandle(
                        openingRange,
                        firstStop
                );

        assertEquals(
                TradeAction.STOP_LOSS_EXIT,
                firstExit.action()
        );

        assertEquals(
                new BigDecimal("-750.90"),
                riskManager.getDailyPnl()
        );

        assertEquals(
                1,
                riskManager.getConsecutiveLosses()
        );

        /*
         * Second losing trade
         */

        Candle secondEntry =
                candle(
                        "2026-01-05T09:33:00",
                        "NIFTY",
                        "25000",
                        "25060",
                        "24990",
                        "25030"
                );

        TradeResult secondOpen =
                tradingEngine.processCandle(
                        openingRange,
                        secondEntry
                );

        assertEquals(
                TradeAction.OPEN_LONG,
                secondOpen.action()
        );

        Candle secondStop =
                candle(
                        "2026-01-05T09:34:00",
                        "NIFTY",
                        "25030",
                        "25040",
                        "24700",
                        "24800"
                );

        TradeResult secondExit =
                tradingEngine.processCandle(
                        openingRange,
                        secondStop
                );

        assertEquals(
                TradeAction.STOP_LOSS_EXIT,
                secondExit.action()
        );

        assertEquals(
                new BigDecimal("-1501.80"),
                riskManager.getDailyPnl()
        );

        assertEquals(
                2,
                riskManager.getConsecutiveLosses()
        );

        /*
         * Third losing trade
         */

        Candle thirdEntry =
                candle(
                        "2026-01-05T09:35:00",
                        "NIFTY",
                        "25000",
                        "25060",
                        "24990",
                        "25030"
                );

        TradeResult thirdOpen =
                tradingEngine.processCandle(
                        openingRange,
                        thirdEntry
                );

        assertEquals(
                TradeAction.OPEN_LONG,
                thirdOpen.action()
        );

        Candle thirdStop =
                candle(
                        "2026-01-05T09:36:00",
                        "NIFTY",
                        "25030",
                        "25040",
                        "24700",
                        "24800"
                );

        TradeResult thirdExit =
                tradingEngine.processCandle(
                        openingRange,
                        thirdStop
                );

        assertEquals(
                TradeAction.STOP_LOSS_EXIT,
                thirdExit.action()
        );

        assertEquals(
                new BigDecimal("-2252.70"),
                riskManager.getDailyPnl()
        );

        assertEquals(
                3,
                riskManager.getConsecutiveLosses()
        );

        /*
         * Fourth trade must be blocked.
         *
         * maxConsecutiveLosses = 3
         */
        Candle fourthEntry =
                candle(
                        "2026-01-05T09:37:00",
                        "NIFTY",
                        "25000",
                        "25060",
                        "24990",
                        "25030"
                );

        TradeResult fourthAttempt =
                tradingEngine.processCandle(
                        openingRange,
                        fourthEntry
                );

        assertEquals(
                TradeAction.NO_ACTION,
                fourthAttempt.action()
        );

        assertNull(
                fourthAttempt.position()
        );

        assertEquals(
                3,
                riskManager.getConsecutiveLosses()
        );
    }

    @Test
    void shouldResetConsecutiveLossesAtStartOfNewTradingDay() {

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

        /*
         * Simulate three consecutive losses
         */
        riskManager.recordTradeResult(
                new BigDecimal("-100")
        );

        riskManager.recordTradeResult(
                new BigDecimal("-100")
        );

        riskManager.recordTradeResult(
                new BigDecimal("-100")
        );

        assertEquals(
                3,
                riskManager.getConsecutiveLosses()
        );

        /*
         * New trading day
         */
        riskManager.resetDailyState();

        assertEquals(
                0,
                riskManager.getConsecutiveLosses()
        );

        assertEquals(
                BigDecimal.ZERO,
                riskManager.getDailyPnl()
        );

        assertEquals(
                0,
                riskManager.getTradesToday()
        );

        /*
         * After reset, a new trade should be allowed.
         */
        assertTrue(
                riskManager.canOpenNewTrade()
        );
    }

    private TradingEngine createTradingEngine(
            RiskConfig riskConfig,
            RiskManager riskManager) {

        return new TradingEngine(
                new BreakoutDetector(),
                new CandleConfirmation(),
                new PositionSizer(),
                riskManager,
                new TrailingStopManager(
                        riskConfig.trailingStopPercent()
                ),
                riskConfig
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