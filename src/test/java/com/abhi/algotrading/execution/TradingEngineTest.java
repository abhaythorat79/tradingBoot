package com.abhi.algotrading.execution;

import com.abhi.algotrading.confirmation.CandleConfirmation;
import com.abhi.algotrading.marketdata.Candle;
import com.abhi.algotrading.portfolio.Position;
import com.abhi.algotrading.risk.PositionSizer;
import com.abhi.algotrading.risk.RiskConfig;
import com.abhi.algotrading.risk.RiskManager;
import com.abhi.algotrading.risk.TrailingStopManager;
import com.abhi.algotrading.strategy.BreakoutDetector;
import com.abhi.algotrading.strategy.OpeningRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TradingEngineTest {

    private TradingEngine tradingEngine;
    private OpeningRange openingRange;

    @BeforeEach
    void setUp() {

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

        tradingEngine =
                new TradingEngine(
                        new BreakoutDetector(),
                        new CandleConfirmation(),
                        new PositionSizer(),
                        riskManager,
                        new TrailingStopManager(
                                riskConfig.trailingStopPercent()
                        ),
                        riskConfig
                );

        openingRange =
                new OpeningRange(
                        LocalDateTime.of(
                                2026, 9, 19, 9, 15
                        ),
                        LocalDateTime.of(
                                2026, 9, 19, 9, 30
                        ),
                        new BigDecimal("100"),
                        new BigDecimal("95")
                );
    }

    @Test
    void shouldOpenLongPositionAfterConfirmedBreakout() {

        Candle candle =
                candle(
                        9, 45,
                        "100",
                        "103",
                        "99",
                        "102",
                        1000
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

        assertNotNull(result.position());

        assertEquals(
                "RELIANCE",
                result.position().getSymbol()
        );

        assertEquals(
                new BigDecimal("102"),
                result.position().getEntryPrice()
        );

        assertEquals(
                980,
                result.position().getQuantity()
        );

        assertTrue(
                result.position().isOpen()
        );
    }

    @Test
    void shouldOpenShortPositionAfterConfirmedBreakout() {

        Candle candle =
                candle(
                        10, 0,
                        "96",
                        "97",
                        "92",
                        "93",
                        1000
                );

        TradeResult result =
                tradingEngine.processCandle(
                        openingRange,
                        candle
                );

        assertEquals(
                TradeAction.OPEN_SHORT,
                result.action()
        );

        assertNotNull(result.position());

        assertEquals(
                new BigDecimal("93"),
                result.position().getEntryPrice()
        );

        assertTrue(
                result.position().isOpen()
        );
    }

    @Test
    void shouldNotTradeWhenThereIsNoBreakout() {

        Candle candle =
                candle(
                        10, 0,
                        "98",
                        "99",
                        "96",
                        "98",
                        1000
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

        assertNull(result.position());
    }

    @Test
    void shouldNotTradeWhenBreakoutIsNotConfirmed() {

        /*
         * High crosses the opening-range high,
         * but candle closes below it.
         */
        Candle candle =
                candle(
                        10, 0,
                        "99",
                        "101",
                        "98",
                        "99.50",
                        1000
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
                tradingEngine.getOpenPosition()
        );
    }

    @Test
    void shouldNotOpenTradeDuringOpeningRange() {

        Candle candle =
                candle(
                        9, 20,
                        "99",
                        "103",
                        "98",
                        "102",
                        1000
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
                tradingEngine.getOpenPosition()
        );
    }

    @Test
    void shouldNotOpenNewTradeAfter1515() {

        Candle candle =
                candle(
                        15, 15,
                        "100",
                        "105",
                        "99",
                        "104",
                        1000
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
                tradingEngine.getOpenPosition()
        );
    }

    @Test
    void shouldActivateTrailingAfterTargetIsReached() {

        /*
         * Entry = 102
         * Initial target = 104.04
         */
        Candle entryCandle =
                candle(
                        9, 45,
                        "100",
                        "103",
                        "99",
                        "102",
                        1000
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

        Position position =
                tradingEngine.getOpenPosition();

        assertNotNull(position);

        assertEquals(
                0,
                new BigDecimal("104.04")
                        .compareTo(position.getCurrentTarget())
        );

        /*
         * Target is reached.
         */
        Candle targetCandle =
                candle(
                        10, 0,
                        "102",
                        "105",
                        "101",
                        "104",
                        1000
                );

        TradeResult targetResult =
                tradingEngine.processCandle(
                        openingRange,
                        targetCandle
                );

        assertEquals(
                TradeAction.TARGET_REACHED,
                targetResult.action()
        );

        assertNull(
                position.getCurrentTarget()
        );

        assertEquals(
                0,
                new BigDecimal("104.04")
                        .compareTo(position.getCurrentStopLoss())
        );
    }

    @Test
    void shouldTrailLongPositionAfterTargetActivation() {

        Candle entryCandle =
                candle(
                        9, 45,
                        "100",
                        "103",
                        "99",
                        "102",
                        1000
                );

        tradingEngine.processCandle(
                openingRange,
                entryCandle
        );

        Position position =
                tradingEngine.getOpenPosition();

        Candle targetCandle =
                candle(
                        10, 0,
                        "102",
                        "105",
                        "101",
                        "104",
                        1000
                );

        tradingEngine.processCandle(
                openingRange,
                targetCandle
        );

        /*
         * Market closes at 106.
         *
         * 1% trailing stop = 104.94
         */
        Candle trailingCandle =
                candle(
                        10, 15,
                        "105",
                        "107",
                        "105",
                        "106",
                        1000
                );

        TradeResult result =
                tradingEngine.processCandle(
                        openingRange,
                        trailingCandle
                );

        assertEquals(
                TradeAction.NO_ACTION,
                result.action()
        );

        assertEquals(
                0,
                new BigDecimal("104.94")
                        .compareTo(position.getCurrentStopLoss())
        );

        assertTrue(
                position.isOpen()
        );
    }

    @Test
    void shouldExitWhenTrailingStopIsHit() {

        Candle entryCandle =
                candle(
                        9, 45,
                        "100",
                        "103",
                        "99",
                        "102",
                        1000
                );

        tradingEngine.processCandle(
                openingRange,
                entryCandle
        );

        Candle targetCandle =
                candle(
                        10, 0,
                        "102",
                        "105",
                        "101",
                        "104",
                        1000
                );

        tradingEngine.processCandle(
                openingRange,
                targetCandle
        );

        /*
         * Close = 106
         * New trailing SL = 104.94
         */
        Candle trailingCandle =
                candle(
                        10, 15,
                        "105",
                        "107",
                        "105",
                        "106",
                        1000
                );

        tradingEngine.processCandle(
                openingRange,
                trailingCandle
        );

        /*
         * Price now falls below trailing SL.
         */
        Candle exitCandle =
                candle(
                        10, 30,
                        "105",
                        "105.5",
                        "104",
                        "104.5",
                        1000
                );

        TradeResult result =
                tradingEngine.processCandle(
                        openingRange,
                        exitCandle
                );

        assertEquals(
                TradeAction.TRAILING_STOP_EXIT,
                result.action()
        );

        assertNotNull(result.position());

        assertFalse(
                result.position().isOpen()
        );

        assertEquals(
                0,
                new BigDecimal("104.94")
                        .compareTo(result.position().getExitPrice())
        );

        assertNull(
                tradingEngine.getOpenPosition()
        );
    }

    @Test
    void shouldForceExitAt1530() {

        Candle entryCandle =
                candle(
                        9, 45,
                        "100",
                        "103",
                        "99",
                        "102",
                        1000
                );

        tradingEngine.processCandle(
                openingRange,
                entryCandle
        );

        assertNotNull(
                tradingEngine.getOpenPosition()
        );

        Candle eodCandle =
                candle(
                        15, 30,
                        "102",
                        "103",
                        "101",
                        "102.50",
                        1000
                );

        TradeResult result =
                tradingEngine.processCandle(
                        openingRange,
                        eodCandle
                );

        assertEquals(
                TradeAction.END_OF_DAY_EXIT,
                result.action()
        );

        assertFalse(
                result.position().isOpen()
        );

        assertEquals(
                new BigDecimal("102.50"),
                result.position().getExitPrice()
        );

        assertNull(
                tradingEngine.getOpenPosition()
        );
    }

    private Candle candle(
            int hour,
            int minute,
            String open,
            String high,
            String low,
            String close,
            long volume) {

        return new Candle(
                LocalDateTime.of(
                        2026,
                        9,
                        19,
                        hour,
                        minute
                ),
                "RELIANCE",
                new BigDecimal(open),
                new BigDecimal(high),
                new BigDecimal(low),
                new BigDecimal(close),
                volume
        );
    }
}