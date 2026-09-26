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

class TradingEngineCapitalUtilizationIntegrationTest {

    @Test
    void shouldOpenTradeWhenPositionIsWithinCapitalUtilizationLimit() {

        RiskConfig riskConfig =
                createRiskConfig();

        RiskManager riskManager =
                new RiskManager(riskConfig);

        TradingEngine tradingEngine =
                createTradingEngine(
                        riskConfig,
                        riskManager,
                        new BigDecimal("80")
                );

        OpeningRange openingRange =
                createOpeningRange();

        /*
         * Entry = 25,050
         *
         * Stop = 24,799.50
         *
         * Risk amount = 1,000
         *
         * Quantity = floor(1000 / 250.50) = 3
         *
         * Position value:
         *
         * 25,050 × 3 = 75,150
         *
         * Maximum allowed:
         *
         * 100,000 × 80% = 80,000
         *
         * Therefore the trade must be allowed.
         */
        Candle candle =
                candle(
                        "2026-01-05T09:31:00",
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

        assertEquals(
                3,
                result.position().getQuantity()
        );

        assertEquals(
                new BigDecimal("75150"),
                result.position()
                        .getEntryPrice()
                        .multiply(
                                BigDecimal.valueOf(
                                        result.position()
                                                .getQuantity()
                                )
                        )
        );

        assertEquals(
                1,
                riskManager.getTradesToday()
        );
    }

    @Test
    void shouldBlockTradeWhenPositionExceedsCapitalUtilizationLimit() {

        RiskConfig riskConfig =
                createRiskConfig();

        RiskManager riskManager =
                new RiskManager(riskConfig);

        TradingEngine tradingEngine =
                createTradingEngine(
                        riskConfig,
                        riskManager,
                        new BigDecimal("80")
                );

        OpeningRange openingRange =
                createOpeningRange();

        /*
         * Entry = 30,000
         *
         * Stop = 29,700
         *
         * Risk amount = 1,000
         *
         * Quantity = floor(1000 / 300) = 3
         *
         * Position value:
         *
         * 30,000 × 3 = 90,000
         *
         * Maximum allowed:
         *
         * 100,000 × 80% = 80,000
         *
         * Therefore the trade must be blocked.
         */
        Candle candle =
                candle(
                        "2026-01-05T09:31:00",
                        "NIFTY",
                        "29900",
                        "30100",
                        "29850",
                        "30000"
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

        /*
         * Capital-utilization rejection must happen
         * before recordTrade().
         */
        assertEquals(
                0,
                riskManager.getTradesToday()
        );
    }

    private TradingEngine createTradingEngine(
            RiskConfig riskConfig,
            RiskManager riskManager,
            BigDecimal maximumUtilizationPercent) {

        CapitalUtilizationConfig utilizationConfig =
                new CapitalUtilizationConfig(
                        maximumUtilizationPercent
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

    private RiskConfig createRiskConfig() {

        return new RiskConfig(
                new BigDecimal("100000"),
                new BigDecimal("1"),
                new BigDecimal("10000"),
                10,
                3,
                new BigDecimal("1"),
                new BigDecimal("2"),
                new BigDecimal("1")
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