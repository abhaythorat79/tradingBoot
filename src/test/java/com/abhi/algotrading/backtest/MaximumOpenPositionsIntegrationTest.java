package com.abhi.algotrading.backtest;

import com.abhi.algotrading.confirmation.CandleConfirmation;
import com.abhi.algotrading.execution.TradeAction;
import com.abhi.algotrading.execution.TradeResult;
import com.abhi.algotrading.execution.TradingEngine;
import com.abhi.algotrading.marketdata.Candle;
import com.abhi.algotrading.portfolio.Position;
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

class MaximumOpenPositionsIntegrationTest {

    @Test
    void shouldNotOpenSecondPositionWhileFirstPositionIsOpen() {

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
         * First valid LONG breakout.
         */
        Candle firstEntry =
                candle(
                        "2026-01-05T09:31:00",
                        "NIFTY",
                        "25000",
                        "25100",
                        "24990",
                        "25050"
                );

        TradeResult firstResult =
                tradingEngine.processCandle(
                        openingRange,
                        firstEntry
                );

        assertEquals(
                TradeAction.OPEN_LONG,
                firstResult.action()
        );

        assertNotNull(
                firstResult.position()
        );

        assertEquals(
                1,
                riskManager.getTradesToday()
        );

        Position firstPosition =
                tradingEngine.getOpenPosition();

        assertNotNull(firstPosition);
        assertTrue(firstPosition.isOpen());

        /*
         * Another LONG breakout occurs while the
         * first LONG position is still open.
         *
         * The engine must manage the existing
         * position instead of opening another one.
         */
        Candle secondEntry =
                candle(
                        "2026-01-05T09:32:00",
                        "NIFTY",
                        "25100",
                        "25200",
                        "25090",
                        "25150"
                );

        TradeResult secondResult =
                tradingEngine.processCandle(
                        openingRange,
                        secondEntry
                );

        assertEquals(
                TradeAction.NO_ACTION,
                secondResult.action()
        );

        /*
         * The existing position is returned.
         */
        assertSame(
                firstPosition,
                secondResult.position()
        );

        /*
         * No second trade was recorded.
         */
        assertEquals(
                1,
                riskManager.getTradesToday()
        );

        /*
         * Original position is still open.
         */
        assertSame(
                firstPosition,
                tradingEngine.getOpenPosition()
        );

        assertTrue(
                tradingEngine
                        .getOpenPosition()
                        .isOpen()
        );
    }

    @Test
    void shouldNotOpenShortPositionInsteadOfManagingExistingLong() {

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
         * Open LONG position first.
         */
        Candle longEntry =
                candle(
                        "2026-01-05T09:31:00",
                        "NIFTY",
                        "25000",
                        "25100",
                        "24990",
                        "25050"
                );

        TradeResult longResult =
                tradingEngine.processCandle(
                        openingRange,
                        longEntry
                );

        assertEquals(
                TradeAction.OPEN_LONG,
                longResult.action()
        );

        Position longPosition =
                tradingEngine.getOpenPosition();

        assertNotNull(longPosition);
        assertTrue(longPosition.isOpen());

        /*
         * A DOWN breakout now occurs.
         *
         * This candle also crosses the existing
         * LONG stop loss, so the correct behavior
         * is to close the LONG position.
         *
         * It must NOT open a SHORT position
         * during the same candle.
         */
        Candle oppositeBreakout =
                candle(
                        "2026-01-05T09:32:00",
                        "NIFTY",
                        "25050",
                        "25060",
                        "24400",
                        "24450"
                );

        TradeResult oppositeResult =
                tradingEngine.processCandle(
                        openingRange,
                        oppositeBreakout
                );

        /*
         * Existing LONG position is stopped out.
         */
        assertEquals(
                TradeAction.STOP_LOSS_EXIT,
                oppositeResult.action()
        );

        /*
         * It must never become OPEN_SHORT.
         */
        assertNotEquals(
                TradeAction.OPEN_SHORT,
                oppositeResult.action()
        );

        /*
         * The LONG position is now closed.
         */
        assertFalse(
                longPosition.isOpen()
        );

        /*
         * TradingEngine has no open position after
         * closing the LONG position.
         */
        assertNull(
                tradingEngine.getOpenPosition()
        );

        /*
         * Only the original trade was opened.
         */
        assertEquals(
                1,
                riskManager.getTradesToday()
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