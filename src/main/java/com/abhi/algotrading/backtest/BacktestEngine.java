package com.abhi.algotrading.backtest;

import com.abhi.algotrading.common.TradingSession;
import com.abhi.algotrading.execution.TradeAction;
import com.abhi.algotrading.execution.TradeResult;
import com.abhi.algotrading.execution.TradingEngine;
import com.abhi.algotrading.marketdata.Candle;
import com.abhi.algotrading.marketdata.MarketDataValidator;
import com.abhi.algotrading.portfolio.Position;
import com.abhi.algotrading.portfolio.PositionSide;
import com.abhi.algotrading.strategy.OpeningRange;
import com.abhi.algotrading.strategy.OpeningRangeCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class BacktestEngine {

    private final MarketDataValidator marketDataValidator;
    private final OpeningRangeCalculator openingRangeCalculator;
    private final TradingEngine tradingEngine;

    public BacktestEngine(
            MarketDataValidator marketDataValidator,
            OpeningRangeCalculator openingRangeCalculator,
            TradingEngine tradingEngine) {

        if (marketDataValidator == null) {
            throw new IllegalArgumentException(
                    "Market data validator cannot be null"
            );
        }

        if (openingRangeCalculator == null) {
            throw new IllegalArgumentException(
                    "Opening range calculator cannot be null"
            );
        }

        if (tradingEngine == null) {
            throw new IllegalArgumentException(
                    "Trading engine cannot be null"
            );
        }

        this.marketDataValidator = marketDataValidator;
        this.openingRangeCalculator = openingRangeCalculator;
        this.tradingEngine = tradingEngine;
    }

    public BacktestResult run(
            List<Candle> candles) {

        marketDataValidator.validate(candles);

        Map<LocalDate, List<Candle>> candlesByDay =
                groupByTradingDay(candles);

        List<BacktestTrade> trades =
                new ArrayList<>();

        for (List<Candle> dayCandles :
                candlesByDay.values()) {

            trades.addAll(
                    processTradingDay(dayCandles)
            );
        }

        return new BacktestResult(
                candlesByDay.size(),
                trades
        );
    }

    private List<BacktestTrade> processTradingDay(
            List<Candle> dayCandles) {

        List<BacktestTrade> trades =
                new ArrayList<>();

        List<Candle> openingCandles =
                dayCandles.stream()
                        .filter(this::isOpeningRangeCandle)
                        .toList();

        if (openingCandles.isEmpty()) {
            return trades;
        }

        OpeningRange openingRange =
                openingRangeCalculator.calculate(
                        dayCandles
                );

        for (Candle candle : dayCandles) {

            /*
             * Do not process the candles that form
             * the 09:15 - 09:30 opening range.
             */
            if (candle.timestamp()
                    .toLocalTime()
                    .isBefore(
                            TradingSession.OPENING_RANGE_END
                    )) {

                continue;
            }

            TradeResult result =
                    tradingEngine.processCandle(
                            openingRange,
                            candle
                    );

            /*
             * TradingEngine returns the Position object
             * inside TradeResult even after that Position
             * has been closed.
             *
             * Therefore, for an exit:
             *
             * result.action() = exit action
             * result.position() = CLOSED Position
             *
             * We use the exit action to identify the
             * completed trade.
             */
            if (isExitAction(result.action())) {

                Position completedPosition =
                        result.position();

                if (completedPosition == null) {

                    throw new IllegalStateException(
                            "Exit action returned without a position: "
                                    + result.action()
                    );
                }

                trades.add(
                        createBacktestTrade(
                                completedPosition,
                                result.action()
                        )
                );
            }
        }

        /*
         * Safety fallback:
         *
         * If the final candle did not trigger the normal
         * EOD exit, close any remaining open position.
         */
        Position remainingPosition =
                tradingEngine.getOpenPosition();

        if (remainingPosition != null &&
                remainingPosition.isOpen()) {

            Candle lastCandle =
                    dayCandles.getLast();

            remainingPosition.close(
                    lastCandle.close(),
                    lastCandle.timestamp()
            );

            trades.add(
                    createBacktestTrade(
                            remainingPosition,
                            TradeAction.END_OF_DAY_EXIT
                    )
            );
        }

        return trades;
    }

    private boolean isExitAction(
            TradeAction action) {

        return action == TradeAction.STOP_LOSS_EXIT
                || action == TradeAction.TRAILING_STOP_EXIT
                || action == TradeAction.END_OF_DAY_EXIT;
    }

    private Map<LocalDate, List<Candle>> groupByTradingDay(
            List<Candle> candles) {

        Map<LocalDate, List<Candle>> result =
                new TreeMap<>();

        for (Candle candle : candles) {

            LocalDate date =
                    candle.timestamp().toLocalDate();

            result.computeIfAbsent(
                    date,
                    ignored -> new ArrayList<>()
            ).add(candle);
        }

        result.values().forEach(
                dayCandles ->
                        dayCandles.sort(
                                Comparator.comparing(
                                        Candle::timestamp
                                )
                        )
        );

        return result;
    }

    private boolean isOpeningRangeCandle(
            Candle candle) {

        return !candle.timestamp()
                .toLocalTime()
                .isBefore(
                        TradingSession.MARKET_OPEN
                )
                &&
                candle.timestamp()
                        .toLocalTime()
                        .isBefore(
                                TradingSession.OPENING_RANGE_END
                        );
    }

    private BacktestTrade createBacktestTrade(
            Position position,
            TradeAction action) {

        BigDecimal entryPrice =
                position.getEntryPrice();

        BigDecimal exitPrice =
                position.getExitPrice();

        if (exitPrice == null) {

            throw new IllegalStateException(
                    "Completed position has no exit price"
            );
        }

        BigDecimal grossPnl =
                calculateGrossPnl(
                        position,
                        exitPrice
                );

        return new BacktestTrade(
                position.getSymbol(),
                position.getSide(),
                position.getQuantity(),
                entryPrice,
                exitPrice,
                position.getEntryTime(),
                position.getExitTime(),
                grossPnl,
                mapExitReason(action)
        );
    }

    private BigDecimal calculateGrossPnl(
            Position position,
            BigDecimal exitPrice) {

        BigDecimal priceDifference;

        if (position.getSide() ==
                PositionSide.LONG) {

            priceDifference =
                    exitPrice.subtract(
                            position.getEntryPrice()
                    );

        } else {

            priceDifference =
                    position.getEntryPrice()
                            .subtract(exitPrice);
        }

        return priceDifference.multiply(
                BigDecimal.valueOf(
                        position.getQuantity()
                )
        );
    }

    private String mapExitReason(
            TradeAction action) {

        return switch (action) {

            case STOP_LOSS_EXIT ->
                    "STOP_LOSS";

            case TRAILING_STOP_EXIT ->
                    "TRAILING_STOP";

            case END_OF_DAY_EXIT ->
                    "END_OF_DAY";

            default ->
                    action.name();
        };
    }
}