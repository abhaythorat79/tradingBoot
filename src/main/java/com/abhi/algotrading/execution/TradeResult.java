package com.abhi.algotrading.execution;

import com.abhi.algotrading.portfolio.Position;

public record TradeResult(
        TradeAction action,
        Position position
) {
}