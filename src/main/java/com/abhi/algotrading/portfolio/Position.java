package com.abhi.algotrading.portfolio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Position {

    private final String symbol;

    private final PositionSide side;

    private final BigDecimal entryPrice;

    private final long quantity;

    private final BigDecimal initialStopLoss;

    private final BigDecimal initialTarget;

    private BigDecimal currentStopLoss;

    private BigDecimal currentTarget;

    private PositionStatus status;

    private LocalDateTime entryTime;

    private LocalDateTime exitTime;

    private BigDecimal exitPrice;

    public Position(
            String symbol,
            PositionSide side,
            BigDecimal entryPrice,
            long quantity,
            BigDecimal initialStopLoss,
            BigDecimal initialTarget,
            LocalDateTime entryTime) {

        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException(
                    "Symbol cannot be empty"
            );
        }

        if (side == null) {
            throw new IllegalArgumentException(
                    "Position side cannot be null"
            );
        }

        if (entryPrice == null || entryPrice.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Entry price must be positive"
            );
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        if (initialStopLoss == null ||
                initialStopLoss.signum() <= 0) {

            throw new IllegalArgumentException(
                    "Initial stop loss must be positive"
            );
        }

        if (initialTarget == null ||
                initialTarget.signum() <= 0) {

            throw new IllegalArgumentException(
                    "Initial target must be positive"
            );
        }

        if (entryTime == null) {
            throw new IllegalArgumentException(
                    "Entry time cannot be null"
            );
        }

        validateInitialRiskLevels(
                side,
                entryPrice,
                initialStopLoss,
                initialTarget
        );

        this.symbol = symbol;
        this.side = side;
        this.entryPrice = entryPrice;
        this.quantity = quantity;

        this.initialStopLoss = initialStopLoss;
        this.initialTarget = initialTarget;

        this.currentStopLoss = initialStopLoss;
        this.currentTarget = initialTarget;

        this.entryTime = entryTime;

        this.status = PositionStatus.OPEN;
    }

    private void validateInitialRiskLevels(
            PositionSide side,
            BigDecimal entryPrice,
            BigDecimal stopLoss,
            BigDecimal target) {

        if (side == PositionSide.LONG) {

            if (stopLoss.compareTo(entryPrice) >= 0) {
                throw new IllegalArgumentException(
                        "For LONG position, stop loss must be below entry price"
                );
            }

            if (target.compareTo(entryPrice) <= 0) {
                throw new IllegalArgumentException(
                        "For LONG position, target must be above entry price"
                );
            }
        }

        if (side == PositionSide.SHORT) {

            if (stopLoss.compareTo(entryPrice) <= 0) {
                throw new IllegalArgumentException(
                        "For SHORT position, stop loss must be above entry price"
                );
            }

            if (target.compareTo(entryPrice) >= 0) {
                throw new IllegalArgumentException(
                        "For SHORT position, target must be below entry price"
                );
            }
        }
    }

    public boolean isOpen() {
        return status == PositionStatus.OPEN;
    }

    public boolean hasFixedTarget() {
        return currentTarget != null;
    }

    public void targetReached() {

        if (!isOpen()) {
            throw new IllegalStateException(
                    "Position is already closed"
            );
        }

        if (currentTarget == null) {
            return;
        }

        /*
         * Once target is reached:
         *
         * Target becomes the new stop loss.
         *
         * There is no fixed target anymore.
         */
        currentStopLoss = currentTarget;

        currentTarget = null;
    }

    public void updateTrailingStop(
            BigDecimal newStopLoss) {

        if (!isOpen()) {
            throw new IllegalStateException(
                    "Position is already closed"
            );
        }

        if (newStopLoss == null ||
                newStopLoss.signum() <= 0) {

            throw new IllegalArgumentException(
                    "Trailing stop loss must be positive"
            );
        }

        if (side == PositionSide.LONG) {

            /*
             * LONG stop can only move UP.
             */
            if (newStopLoss.compareTo(currentStopLoss) > 0) {
                currentStopLoss = newStopLoss;
            }
        }

        if (side == PositionSide.SHORT) {

            /*
             * SHORT stop can only move DOWN.
             */
            if (newStopLoss.compareTo(currentStopLoss) < 0) {
                currentStopLoss = newStopLoss;
            }
        }
    }

    public boolean isStopLossHit(
            BigDecimal marketPrice) {

        if (!isOpen()) {
            return false;
        }

        if (marketPrice == null) {
            throw new IllegalArgumentException(
                    "Market price cannot be null"
            );
        }

        if (side == PositionSide.LONG) {

            return marketPrice.compareTo(
                    currentStopLoss
            ) <= 0;
        }

        return marketPrice.compareTo(
                currentStopLoss
        ) >= 0;
    }

    public void close(
            BigDecimal exitPrice,
            LocalDateTime exitTime) {

        if (!isOpen()) {
            throw new IllegalStateException(
                    "Position is already closed"
            );
        }

        if (exitPrice == null ||
                exitPrice.signum() <= 0) {

            throw new IllegalArgumentException(
                    "Exit price must be positive"
            );
        }

        if (exitTime == null) {
            throw new IllegalArgumentException(
                    "Exit time cannot be null"
            );
        }

        this.exitPrice = exitPrice;
        this.exitTime = exitTime;
        this.status = PositionStatus.CLOSED;
    }

    public BigDecimal unrealizedPnl(
            BigDecimal marketPrice) {

        if (!isOpen()) {
            throw new IllegalStateException(
                    "Position is not open"
            );
        }

        if (marketPrice == null) {
            throw new IllegalArgumentException(
                    "Market price cannot be null"
            );
        }

        BigDecimal priceDifference;

        if (side == PositionSide.LONG) {

            priceDifference =
                    marketPrice.subtract(entryPrice);

        } else {

            priceDifference =
                    entryPrice.subtract(marketPrice);
        }

        return priceDifference.multiply(
                BigDecimal.valueOf(quantity)
        );
    }

    public String getSymbol() {
        return symbol;
    }

    public PositionSide getSide() {
        return side;
    }

    public BigDecimal getEntryPrice() {
        return entryPrice;
    }

    public long getQuantity() {
        return quantity;
    }

    public BigDecimal getInitialStopLoss() {
        return initialStopLoss;
    }

    public BigDecimal getInitialTarget() {
        return initialTarget;
    }

    public BigDecimal getCurrentStopLoss() {
        return currentStopLoss;
    }

    public BigDecimal getCurrentTarget() {
        return currentTarget;
    }

    public PositionStatus getStatus() {
        return status;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public BigDecimal getExitPrice() {
        return exitPrice;
    }
}