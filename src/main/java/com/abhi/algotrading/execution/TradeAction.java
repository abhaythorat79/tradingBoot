package com.abhi.algotrading.execution;

public enum TradeAction {

    NO_ACTION,

    OPEN_LONG,

    OPEN_SHORT,

    TARGET_REACHED,

    STOP_LOSS_EXIT,

    TRAILING_STOP_EXIT,

    END_OF_DAY_EXIT
}