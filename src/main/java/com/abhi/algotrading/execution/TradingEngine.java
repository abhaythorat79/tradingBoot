package com.abhi.algotrading.execution;

import com.abhi.algotrading.common.TradingSession;
import com.abhi.algotrading.confirmation.CandleConfirmation;
import com.abhi.algotrading.confirmation.ConfirmationResult;
import com.abhi.algotrading.marketdata.Candle;
import com.abhi.algotrading.portfolio.Position;
import com.abhi.algotrading.portfolio.PositionSide;
import com.abhi.algotrading.risk.PositionSizer;
import com.abhi.algotrading.risk.RiskConfig;
import com.abhi.algotrading.risk.RiskManager;
import com.abhi.algotrading.risk.TrailingStopManager;
import com.abhi.algotrading.strategy.BreakoutDetector;
import com.abhi.algotrading.strategy.BreakoutDirection;
import com.abhi.algotrading.strategy.OpeningRange;

import java.math.BigDecimal;

public class TradingEngine {

    private final BreakoutDetector breakoutDetector;
    private final CandleConfirmation candleConfirmation;
    private final PositionSizer positionSizer;
    private final RiskManager riskManager;
    private final TrailingStopManager trailingStopManager;
    private final RiskConfig riskConfig;

    private Position openPosition;

    public TradingEngine(
            BreakoutDetector breakoutDetector,
            CandleConfirmation candleConfirmation,
            PositionSizer positionSizer,
            RiskManager riskManager,
            TrailingStopManager trailingStopManager,
            RiskConfig riskConfig) {

        if (breakoutDetector == null) {
            throw new IllegalArgumentException(
                    "Breakout detector cannot be null"
            );
        }

        if (candleConfirmation == null) {
            throw new IllegalArgumentException(
                    "Candle confirmation cannot be null"
            );
        }

        if (positionSizer == null) {
            throw new IllegalArgumentException(
                    "Position sizer cannot be null"
            );
        }

        if (riskManager == null) {
            throw new IllegalArgumentException(
                    "Risk manager cannot be null"
            );
        }

        if (trailingStopManager == null) {
            throw new IllegalArgumentException(
                    "Trailing stop manager cannot be null"
            );
        }

        if (riskConfig == null) {
            throw new IllegalArgumentException(
                    "Risk configuration cannot be null"
            );
        }

        this.breakoutDetector = breakoutDetector;
        this.candleConfirmation = candleConfirmation;
        this.positionSizer = positionSizer;
        this.riskManager = riskManager;
        this.trailingStopManager = trailingStopManager;
        this.riskConfig = riskConfig;
    }

    public TradeResult processCandle(
            OpeningRange openingRange,
            Candle candle) {

        if (openingRange == null) {
            throw new IllegalArgumentException(
                    "Opening range cannot be null"
            );
        }

        if (candle == null) {
            throw new IllegalArgumentException(
                    "Candle cannot be null"
            );
        }

        /*
         * Existing position always gets priority.
         *
         * We must manage an existing position even
         * after new-entry time has ended.
         */
        if (openPosition != null &&
                openPosition.isOpen()) {

            return manageOpenPosition(candle);
        }

        /*
         * No new trades outside the permitted
         * entry windows.
         */
        if (!TradingSession.isNewTradeAllowed(
                candle.timestamp().toLocalTime())) {

            return new TradeResult(
                    TradeAction.NO_ACTION,
                    null
            );
        }

        /*
         * Risk management is a hard entry gate.
         */
        if (!riskManager.canOpenNewTrade()) {

            return new TradeResult(
                    TradeAction.NO_ACTION,
                    null
            );
        }

        return evaluateNewEntry(
                openingRange,
                candle
        );
    }

    private TradeResult evaluateNewEntry(
            OpeningRange openingRange,
            Candle candle) {

        BreakoutDirection direction =
                breakoutDetector.detect(
                        openingRange,
                        candle
                );

        /*
         * A candle that breaks both sides does not
         * produce an entry.
         */
        if (direction != BreakoutDirection.UP &&
                direction != BreakoutDirection.DOWN) {

            return new TradeResult(
                    TradeAction.NO_ACTION,
                    null
            );
        }

        ConfirmationResult confirmation =
                candleConfirmation.confirm(
                        openingRange,
                        candle,
                        direction
                );

        if (confirmation !=
                ConfirmationResult.CONFIRMED) {

            return new TradeResult(
                    TradeAction.NO_ACTION,
                    null
            );
        }

        PositionSide side =
                direction == BreakoutDirection.UP
                        ? PositionSide.LONG
                        : PositionSide.SHORT;

        BigDecimal entryPrice =
                candle.close();

        BigDecimal stopLoss =
                calculateInitialStopLoss(
                        side,
                        entryPrice
                );

        BigDecimal target =
                calculateInitialTarget(
                        side,
                        entryPrice
                );

        long quantity =
                positionSizer.calculateQuantity(
                        riskConfig.capital(),
                        riskConfig.riskPerTradePercent(),
                        entryPrice,
                        stopLoss
                );

        if (quantity <= 0) {
            return new TradeResult(
                    TradeAction.NO_ACTION,
                    null
            );
        }

        openPosition =
                new Position(
                        candle.symbol(),
                        side,
                        entryPrice,
                        quantity,
                        stopLoss,
                        target,
                        candle.timestamp()
                );

        riskManager.recordTrade();

        TradeAction action =
                side == PositionSide.LONG
                        ? TradeAction.OPEN_LONG
                        : TradeAction.OPEN_SHORT;

        return new TradeResult(
                action,
                openPosition
        );
    }

    private TradeResult manageOpenPosition(
            Candle candle) {

        /*
         * Hard EOD rule.
         *
         * At 15:30 all positions must be closed.
         */
        if (TradingSession.isEndOfDay(
                candle.timestamp().toLocalTime())) {

            openPosition.close(
                    candle.close(),
                    candle.timestamp()
            );

            TradeResult result =
                    new TradeResult(
                            TradeAction.END_OF_DAY_EXIT,
                            openPosition
                    );

            openPosition = null;

            return result;
        }

        /*
         * Before target activation:
         *
         * We check stop and target using candle
         * high/low because either level may be
         * reached intrabar.
         *
         * If both are touched in the same candle,
         * exact order cannot be known from OHLC data.
         *
         * We use the conservative STOP-FIRST rule.
         */
        if (openPosition.hasFixedTarget()) {

            if (isStopLossHit(
                    openPosition,
                    candle)) {

                BigDecimal exitPrice =
                        openPosition.getCurrentStopLoss();

                openPosition.close(
                        exitPrice,
                        candle.timestamp()
                );

                TradeResult result =
                        new TradeResult(
                                TradeAction.STOP_LOSS_EXIT,
                                openPosition
                        );

                openPosition = null;

                return result;
            }

            if (isTargetHit(
                    openPosition,
                    candle)) {

                openPosition.targetReached();

                return new TradeResult(
                        TradeAction.TARGET_REACHED,
                        openPosition
                );
            }

            return new TradeResult(
                    TradeAction.NO_ACTION,
                    openPosition
            );
        }

        /*
         * After target activation, the target has
         * become the stop loss.
         *
         * Now trailing stop is allowed.
         */
        trailingStopManager.update(
                openPosition,
                candle.close()
        );

        if (isStopLossHit(
                openPosition,
                candle)) {

            BigDecimal exitPrice =
                    openPosition.getCurrentStopLoss();

            openPosition.close(
                    exitPrice,
                    candle.timestamp()
            );

            TradeResult result =
                    new TradeResult(
                            TradeAction.TRAILING_STOP_EXIT,
                            openPosition
                    );

            openPosition = null;

            return result;
        }

        return new TradeResult(
                TradeAction.NO_ACTION,
                openPosition
        );
    }

    private boolean isTargetHit(
            Position position,
            Candle candle) {

        if (!position.hasFixedTarget()) {
            return false;
        }

        BigDecimal target =
                position.getCurrentTarget();

        if (position.getSide() ==
                PositionSide.LONG) {

            return candle.high().compareTo(
                    target
            ) >= 0;
        }

        return candle.low().compareTo(
                target
        ) <= 0;
    }

    private boolean isStopLossHit(
            Position position,
            Candle candle) {

        BigDecimal stopLoss =
                position.getCurrentStopLoss();

        if (position.getSide() ==
                PositionSide.LONG) {

            return candle.low().compareTo(
                    stopLoss
            ) <= 0;
        }

        return candle.high().compareTo(
                stopLoss
        ) >= 0;
    }

    private BigDecimal calculateInitialStopLoss(
            PositionSide side,
            BigDecimal entryPrice) {

        BigDecimal percentage =
                riskConfig.stopLossPercent()
                        .divide(
                                new BigDecimal("100")
                        );

        BigDecimal amount =
                entryPrice.multiply(
                        percentage
                );

        if (side == PositionSide.LONG) {
            return entryPrice.subtract(amount);
        }

        return entryPrice.add(amount);
    }

    private BigDecimal calculateInitialTarget(
            PositionSide side,
            BigDecimal entryPrice) {

        BigDecimal percentage =
                riskConfig.targetPercent()
                        .divide(
                                new BigDecimal("100")
                        );

        BigDecimal amount =
                entryPrice.multiply(
                        percentage
                );

        if (side == PositionSide.LONG) {
            return entryPrice.add(amount);
        }

        return entryPrice.subtract(amount);
    }

    public Position getOpenPosition() {
        return openPosition;
    }
}