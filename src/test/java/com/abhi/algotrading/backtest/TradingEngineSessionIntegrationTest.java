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

class TradingEngineSessionIntegrationTest {

    @Test
    void shouldNotOpenTradeBeforeOpeningRangeEnds() {

        TradingEngine tradingEngine =
                createTradingEngine();

        OpeningRange openingRange =
                createOpeningRange();

        Candle candle =
                candle(
                        "2026-01-05T09:29:59",
                        "NIFTY",
                        "24900",
                        "25100",
                        "24800",
                        "25050"
                );

        TradeResult result =
                tradingEngine.processCandle(
                        openingRange,
                        candle
                );

        assertEquals(
                TradeAction.NO_ACTION,
                result.action()
        );

        assertNull(
                result.position()
        );
    }

    @Test
    void shouldAllowTradeAt0930() {

        TradingEngine tradingEngine =
                createTradingEngine();

        OpeningRange openingRange =
                createOpeningRange();

        Candle candle =
                candle(
                        "2026-01-05T09:30:00",
                        "NIFTY",
                        "25000",
                        "25100",
                        "24990",
                        "25050"
                );

        TradeResult result =
                tradingEngine.processCandle(
                        openingRange,
                        candle
                );

        assertEquals(
                TradeAction.OPEN_LONG,
                result.action()
        );

        assertNotNull(
                result.position()
        );
    }

    @Test
    void shouldAllowTradeAt151459() {

        TradingEngine tradingEngine =
                createTradingEngine();

        OpeningRange openingRange =
                createOpeningRange();

        Candle candle =
                candle(
                        "2026-01-05T15:14:59",
                        "NIFTY",
                        "25000",
                        "25100",
                        "24990",
                        "25050"
                );

        TradeResult result =
                tradingEngine.processCandle(
                        openingRange,
                        candle
                );

        assertEquals(
                TradeAction.OPEN_LONG,
                result.action()
        );

        assertNotNull(
                result.position()
        );
    }

    @Test
    void shouldBlockNewTradeAt1515() {

        TradingEngine tradingEngine =
                createTradingEngine();

        OpeningRange openingRange =
                createOpeningRange();

        Candle candle =
                candle(
                        "2026-01-05T15:15:00",
                        "NIFTY",
                        "25000",
                        "25100",
                        "24990",
                        "25050"
                );

        TradeResult result =
                tradingEngine.processCandle(
                        openingRange,
                        candle
                );

        assertEquals(
                TradeAction.NO_ACTION,
                result.action()
        );

        assertNull(
                result.position()
        );

        assertNull(
                tradingEngine.getOpenPosition()
        );
    }

    @Test
    void shouldBlockNewTradeAfter1515() {

        TradingEngine tradingEngine =
                createTradingEngine();

        OpeningRange openingRange =
                createOpeningRange();

        Candle candle =
                candle(
                        "2026-01-05T15:20:00",
                        "NIFTY",
                        "25000",
                        "25100",
                        "24990",
                        "25050"
                );

        TradeResult result =
                tradingEngine.processCandle(
                        openingRange,
                        candle
                );

        assertEquals(
                TradeAction.NO_ACTION,
                result.action()
        );

        assertNull(
                result.position()
        );
    }

    @Test
    void shouldCloseExistingPositionAt1530() {

        TradingEngine tradingEngine =
                createTradingEngine();

        OpeningRange openingRange =
                createOpeningRange();

        /*
         * First open a position during the
         * allowed entry window.
         */
        Candle entryCandle =
                candle(
                        "2026-01-05T15:14:59",
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
         * At 15:30 all positions must be closed.
         */
        Candle endOfDayCandle =
                candle(
                        "2026-01-05T15:30:00",
                        "NIFTY",
                        "25050",
                        "25070",
                        "25020",
                        "25040"
                );

        TradeResult exitResult =
                tradingEngine.processCandle(
                        openingRange,
                        endOfDayCandle
                );

        assertEquals(
                TradeAction.END_OF_DAY_EXIT,
                exitResult.action()
        );

        assertNotNull(
                exitResult.position()
        );

        assertFalse(
                exitResult.position().isOpen()
        );

        assertNull(
                tradingEngine.getOpenPosition()
        );
    }

    @Test
    void shouldManageExistingPositionAfter1515InsteadOfOpeningAnotherTrade() {

        TradingEngine tradingEngine =
                createTradingEngine();

        OpeningRange openingRange =
                createOpeningRange();

        /*
         * Open a position before the cutoff.
         */
        Candle entryCandle =
                candle(
                        "2026-01-05T15:14:59",
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

        /*
         * After 15:15 the engine must continue
         * managing the existing position.
         *
         * It must NOT try to open another trade.
         */
        Candle managementCandle =
                candle(
                        "2026-01-05T15:20:00",
                        "NIFTY",
                        "25050",
                        "25080",
                        "25020",
                        "25060"
                );

        TradeResult managementResult =
                tradingEngine.processCandle(
                        openingRange,
                        managementCandle
                );

        assertNotEquals(
                TradeAction.OPEN_LONG,
                managementResult.action()
        );

        assertNotEquals(
                TradeAction.OPEN_SHORT,
                managementResult.action()
        );

        assertNotNull(
                tradingEngine.getOpenPosition()
        );

        assertTrue(
                tradingEngine
                        .getOpenPosition()
                        .isOpen()
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

        CapitalUtilizationConfig
                utilizationConfig =
                new CapitalUtilizationConfig(
                        new BigDecimal("100")
                );

        CapitalUtilizationCalculator
                utilizationCalculator =
                new CapitalUtilizationCalculator(
                        utilizationConfig
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