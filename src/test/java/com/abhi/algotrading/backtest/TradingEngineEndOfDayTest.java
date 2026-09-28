package com.abhi.algotrading.execution;

import com.abhi.algotrading.confirmation.CandleConfirmation;
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

class TradingEngineEndOfDayTest {

    @Test
    void shouldCloseOpenPositionAtMarketClose() {

        TradingEngine tradingEngine =
                createTradingEngine();

        OpeningRange openingRange =
                createOpeningRange();

        /*
         * 09:31 candle breaks above the
         * opening range and closes above it.
         *
         * This should open a LONG position.
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
         * 15:30 candle.
         *
         * The position must be closed at the
         * candle close because the market session
         * has ended.
         */
        Candle marketCloseCandle =
                candle(
                        "2026-01-05T15:30:00",
                        "NIFTY",
                        "25100",
                        "25200",
                        "25050",
                        "25150"
                );

        TradeResult result =
                tradingEngine.processCandle(
                        openingRange,
                        marketCloseCandle
                );

        assertEquals(
                TradeAction.END_OF_DAY_EXIT,
                result.action()
        );

        assertNotNull(
                result.position()
        );

        assertFalse(
                result.position().isOpen()
        );

        assertEquals(
                new BigDecimal("25150"),
                result.position().getExitPrice()
        );

        assertEquals(
                LocalDateTime.of(
                        2026, 1, 5, 15, 30
                ),
                result.position().getExitTime()
        );

        /*
         * No position should remain open
         * after the market close.
         */
        assertNull(
                tradingEngine.getOpenPosition()
        );
    }

    @Test
    void shouldAlsoClosePositionWhenCandleTimeIsAfterMarketClose() {

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

        /*
         * Any timestamp >= 15:30 is considered
         * end-of-day by TradingSession.
         */
        Candle afterMarketCloseCandle =
                candle(
                        "2026-01-05T15:31:00",
                        "NIFTY",
                        "25100",
                        "25200",
                        "25050",
                        "25125"
                );

        TradeResult result =
                tradingEngine.processCandle(
                        openingRange,
                        afterMarketCloseCandle
                );

        assertEquals(
                TradeAction.END_OF_DAY_EXIT,
                result.action()
        );

        assertFalse(
                result.position().isOpen()
        );

        assertEquals(
                new BigDecimal("25125"),
                result.position().getExitPrice()
        );

        assertNull(
                tradingEngine.getOpenPosition()
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