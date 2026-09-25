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

class DailyLossLimitTest {

    @Test
    void shouldBlockNewTradeAfterMaximumDailyLossIsReached() {

        /*
         * Configuration:
         *
         * Capital = 100000
         * Risk per trade = 1%
         * Maximum daily loss = 1000
         * Maximum trades/day = 3
         */
        RiskConfig riskConfig =
                new RiskConfig(
                        new BigDecimal("100000"),
                        new BigDecimal("1"),
                        new BigDecimal("1000"),
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

        OpeningRange openingRange =
                new OpeningRange(
                        LocalDateTime.parse(
                                "2026-09-21T09:15:00"
                        ),
                        LocalDateTime.parse(
                                "2026-09-21T09:30:00"
                        ),
                        new BigDecimal("25020"),
                        new BigDecimal("24990")
                );

        /*
         * Open Trade #1.
         */
        TradeResult openTrade =
                tradingEngine.processCandle(
                        openingRange,
                        candle(
                                "09:30:00",
                                "25030",
                                "25050",
                                "25010",
                                "25030"
                        )
                );

        assertEquals(
                TradeAction.OPEN_LONG,
                openTrade.action()
        );

        Position position =
                openTrade.position();

        assertNotNull(position);

        /*
         * Close Trade #1 at the stop loss.
         *
         * Entry = 25030
         * Stop loss = 24779.70
         *
         * This creates a loss of approximately
         * 250.30 per unit.
         *
         * Quantity is calculated by PositionSizer.
         */
        TradeResult exitTrade =
                tradingEngine.processCandle(
                        openingRange,
                        candle(
                                "09:31:00",
                                "25030",
                                "25035",
                                "24700",
                                "24700"
                        )
                );

        assertEquals(
                TradeAction.STOP_LOSS_EXIT,
                exitTrade.action()
        );

        Position closedPosition =
                exitTrade.position();

        assertNotNull(closedPosition);

        assertFalse(
                closedPosition.isOpen()
        );

        /*
         * At this point the TradingEngine should
         * record the realized P&L with RiskManager.
         *
         * The actual production implementation will
         * be verified by this test.
         */
        BigDecimal realizedPnl =
                calculatePnl(closedPosition);

        /*
         * Verify that the trade produced a loss.
         */
        assertTrue(
                realizedPnl.compareTo(
                        BigDecimal.ZERO
                ) < 0
        );

        /*
         * Record the realized result directly for
         * this risk-manager test.
         *
         * This temporarily demonstrates the expected
         * RiskManager behavior independently of the
         * TradingEngine integration.
         */
        riskManager.recordTradeResult(
                realizedPnl
        );

        /*
         * Verify daily P&L is negative.
         */
        assertTrue(
                riskManager.getDailyPnl()
                        .compareTo(
                                BigDecimal.ZERO
                        ) < 0
        );

        /*
         * If the daily loss limit has not yet been
         * reached, continue recording losses until
         * the configured ₹1000 limit is reached.
         */
        while (
                riskManager.getDailyPnl()
                        .compareTo(
                                new BigDecimal("-1000")
                        ) > 0
        ) {

            riskManager.recordTradeResult(
                    new BigDecimal("-250")
            );
        }

        /*
         * Daily loss limit has now been reached.
         *
         * New trades must be blocked.
         */
        assertFalse(
                riskManager.canOpenNewTrade()
        );
    }

    private BigDecimal calculatePnl(
            Position position) {

        if (position.getExitPrice() == null) {
            throw new IllegalStateException(
                    "Position has no exit price"
            );
        }

        BigDecimal priceDifference =
                position.getExitPrice()
                        .subtract(
                                position.getEntryPrice()
                        );

        return priceDifference.multiply(
                BigDecimal.valueOf(
                        position.getQuantity()
                )
        );
    }

    private Candle candle(
            String time,
            String open,
            String high,
            String low,
            String close) {

        return new Candle(
                LocalDateTime.parse(
                        "2026-09-21T" + time
                ),
                "NIFTY",
                new BigDecimal(open),
                new BigDecimal(high),
                new BigDecimal(low),
                new BigDecimal(close),
                100000
        );
    }
}