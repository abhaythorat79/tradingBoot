package com.abhi.algotrading.backtest;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionCostModelTest {

    @Test
    void shouldCalculateOrderValue() {

        TransactionCostModel model =
                new TransactionCostModel(
                        new BigDecimal("0.10"),
                        new BigDecimal("20"),
                        new BigDecimal("0.01"),
                        new BigDecimal("5")
                );

        BigDecimal result =
                model.calculateOrderValue(
                        new BigDecimal("100"),
                        10
                );

        assertEquals(
                0,
                result.compareTo(
                        new BigDecimal("1000")
                )
        );
    }

    @Test
    void shouldCalculatePercentageBrokerage() {

        TransactionCostModel model =
                new TransactionCostModel(
                        new BigDecimal("0.10"),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                );

        BigDecimal result =
                model.calculateBrokerage(
                        new BigDecimal("100"),
                        10
                );

        /*
         * Order value = 100 × 10 = 1000
         *
         * Brokerage = 1000 × 0.10%
         *           = 1
         */
        assertEquals(
                0,
                result.compareTo(
                        new BigDecimal("1")
                )
        );
    }

    @Test
    void shouldCalculateFixedBrokerage() {

        TransactionCostModel model =
                new TransactionCostModel(
                        BigDecimal.ZERO,
                        new BigDecimal("20"),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                );

        BigDecimal result =
                model.calculateBrokerage(
                        new BigDecimal("100"),
                        10
                );

        assertEquals(
                0,
                result.compareTo(
                        new BigDecimal("20")
                )
        );
    }

    @Test
    void shouldCalculateTransactionCharge() {

        TransactionCostModel model =
                new TransactionCostModel(
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        new BigDecimal("0.10"),
                        BigDecimal.ZERO
                );

        BigDecimal result =
                model.calculateTransactionCharge(
                        new BigDecimal("100"),
                        10
                );

        /*
         * Order value = 1000
         *
         * Charge = 1000 × 0.10%
         *        = 1
         */
        assertEquals(
                0,
                result.compareTo(
                        new BigDecimal("1")
                )
        );
    }

    @Test
    void shouldCalculateAdditionalFixedCharge() {

        TransactionCostModel model =
                new TransactionCostModel(
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        new BigDecimal("5")
                );

        BigDecimal result =
                model.calculateAdditionalCharge(
                        new BigDecimal("100"),
                        10
                );

        assertEquals(
                0,
                result.compareTo(
                        new BigDecimal("5")
                )
        );
    }

    @Test
    void shouldCalculateTotalOrderCost() {

        TransactionCostModel model =
                new TransactionCostModel(
                        new BigDecimal("0.10"),
                        new BigDecimal("20"),
                        new BigDecimal("0.10"),
                        new BigDecimal("5")
                );

        BigDecimal result =
                model.calculateOrderCost(
                        new BigDecimal("100"),
                        10
                );

        /*
         * Order value = 1000
         *
         * Brokerage percentage = 1
         * Fixed brokerage      = 20
         * Transaction charge   = 1
         * Additional charge    = 5
         *
         * Total = 27
         */
        assertEquals(
                0,
                result.compareTo(
                        new BigDecimal("27")
                )
        );
    }

    @Test
    void shouldCalculateRoundTripCost() {

        TransactionCostModel model =
                new TransactionCostModel(
                        new BigDecimal("0.10"),
                        new BigDecimal("20"),
                        new BigDecimal("0.10"),
                        new BigDecimal("5")
                );

        BigDecimal result =
                model.calculateRoundTripCost(
                        new BigDecimal("100"),
                        new BigDecimal("110"),
                        10
                );

        /*
         * Entry:
         * 100 × 10 = 1000
         * Cost = 27
         *
         * Exit:
         * 110 × 10 = 1100
         *
         * Brokerage percentage = 1.10
         * Fixed brokerage      = 20
         * Transaction charge   = 1.10
         * Additional charge    = 5
         *
         * Exit cost = 27.20
         *
         * Round trip = 27 + 27.20
         *            = 54.20
         */
        assertEquals(
                0,
                result.compareTo(
                        new BigDecimal("54.20")
                )
        );
    }

    @Test
    void shouldRejectNullBrokeragePercentage() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new TransactionCostModel(
                        null,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                )
        );
    }

    @Test
    void shouldRejectNegativeTransactionCharge() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new TransactionCostModel(
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        new BigDecimal("-0.10"),
                        BigDecimal.ZERO
                )
        );
    }

    @Test
    void shouldRejectInvalidExecutionPrice() {

        TransactionCostModel model =
                new TransactionCostModel(
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> model.calculateOrderCost(
                        BigDecimal.ZERO,
                        10
                )
        );
    }

    @Test
    void shouldRejectInvalidQuantity() {

        TransactionCostModel model =
                new TransactionCostModel(
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> model.calculateOrderCost(
                        new BigDecimal("100"),
                        0
                )
        );
    }
}