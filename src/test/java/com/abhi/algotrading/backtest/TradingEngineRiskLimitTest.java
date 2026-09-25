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

class TradingEngineRiskLimitTest {

    @Test
    void shouldStopOpeningNewTradesAfterMaximumTradesPerDay() {

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
         * Trade #1
         */
        TradeResult firstTrade =
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
                firstTrade.action()
        );

        Position firstPosition =
                firstTrade.position();

        assertNotNull(firstPosition);

        /*
         * Close Trade #1 using stop loss.
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
         * Trade #2
         */
        TradeResult secondTrade =
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
                secondTrade.action()
        );

        /*
         * Close Trade #2.
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
         * Trade #3
         */
        TradeResult thirdTrade =
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
                TradeAction.OPEN_LONG,
                thirdTrade.action()
        );

        /*
         * Close Trade #3.
         */
        TradeResult thirdExit =
                tradingEngine.processCandle(
                        openingRange,
                        candle(
                                "09:35:00",
                                "25030",
                                "25035",
                                "24700",
                                "24700"
                        )
                );

        assertEquals(
                TradeAction.STOP_LOSS_EXIT,
                thirdExit.action()
        );

        /*
         * Trade #4 MUST NOT be allowed.
         *
         * Maximum trades per day = 3.
         */
        TradeResult fourthTrade =
                tradingEngine.processCandle(
                        openingRange,
                        candle(
                                "09:36:00",
                                "25030",
                                "25050",
                                "25010",
                                "25030"
                        )
                );

        assertEquals(
                TradeAction.NO_ACTION,
                fourthTrade.action()
        );

        assertNull(
                fourthTrade.position()
        );

        /*
         * Verify RiskManager state.
         */
        assertEquals(
                3,
                riskManager.getTradesToday()
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