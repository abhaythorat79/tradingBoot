package com.abhi.algotrading.backtest;

import com.abhi.algotrading.confirmation.CandleConfirmation;
import com.abhi.algotrading.execution.TradeAction;
import com.abhi.algotrading.execution.TradeResult;
import com.abhi.algotrading.execution.TradingEngine;
import com.abhi.algotrading.marketdata.Candle;
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

class DailyLossLimitIntegrationTest {

    @Test
    void shouldAutomaticallyRecordLossAndBlockNewTradeAfterDailyLossLimit() {

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
         * =====================================================
         * TRADE #1
         * =====================================================
         */

        TradeResult firstEntry =
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
                firstEntry.action()
        );

        assertNotNull(
                firstEntry.position()
        );

        /*
         * Entry = 25030
         *
         * Initial stop loss:
         *
         * 25030 - 1%
         * = 24779.70
         *
         * Risk per unit:
         *
         * 25030 - 24779.70
         * = 250.30
         *
         * Quantity:
         *
         * 1000 / 250.30
         * = 3 units
         *
         * Expected loss:
         *
         * 250.30 × 3
         * = 750.90
         */

        TradeResult firstExit =
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
                firstExit.action()
        );

        /*
         * TradingEngine must automatically record
         * the realized P&L.
         */
        assertEquals(
                0,
                riskManager.getDailyPnl()
                        .compareTo(
                                new BigDecimal("-750.90")
                        )
        );

        /*
         * Daily loss limit is ₹1000.
         *
         * Current loss = ₹750.90
         *
         * Therefore another trade is still allowed.
         */
        assertTrue(
                riskManager.canOpenNewTrade()
        );

        /*
         * =====================================================
         * TRADE #2
         * =====================================================
         */

        TradeResult secondEntry =
                tradingEngine.processCandle(
                        openingRange,
                        candle(
                                "09:32:00",
                                "25030",
                                "25050",
                                "25010",
                                "25030"
                        )
                );

        assertEquals(
                TradeAction.OPEN_LONG,
                secondEntry.action()
        );

        assertNotNull(
                secondEntry.position()
        );

        /*
         * Close second trade at stop loss.
         */
        TradeResult secondExit =
                tradingEngine.processCandle(
                        openingRange,
                        candle(
                                "09:33:00",
                                "25030",
                                "25035",
                                "24700",
                                "24700"
                        )
                );

        assertEquals(
                TradeAction.STOP_LOSS_EXIT,
                secondExit.action()
        );

        /*
         * Total daily loss:
         *
         * -750.90
         * -750.90
         * --------
         * -1501.80
         *
         * This is beyond the -1000 limit.
         */
        assertEquals(
                0,
                riskManager.getDailyPnl()
                        .compareTo(
                                new BigDecimal("-1501.80")
                        )
        );

        assertTrue(
                riskManager.getDailyPnl()
                        .compareTo(
                                new BigDecimal("-1000")
                        ) <= 0
        );

        /*
         * =====================================================
         * TRADE #3
         * =====================================================
         *
         * The daily loss limit has been reached.
         *
         * Therefore the third trade MUST be blocked.
         */

        TradeResult thirdEntry =
                tradingEngine.processCandle(
                        openingRange,
                        candle(
                                "09:34:00",
                                "25030",
                                "25050",
                                "25010",
                                "25030"
                        )
                );

        assertEquals(
                TradeAction.NO_ACTION,
                thirdEntry.action()
        );

        assertNull(
                thirdEntry.position()
        );

        /*
         * Only two trades were opened.
         */
        assertEquals(
                2,
                riskManager.getTradesToday()
        );

        /*
         * Risk manager must reject another trade.
         */
        assertFalse(
                riskManager.canOpenNewTrade()
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