package com.abhi.algotrading.backtest;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TransactionCostModel {

    private final BigDecimal brokeragePercent;
    private final BigDecimal fixedBrokeragePerOrder;
    private final BigDecimal transactionChargePercent;
    private final BigDecimal fixedAdditionalChargePerOrder;

    public TransactionCostModel(
            BigDecimal brokeragePercent,
            BigDecimal fixedBrokeragePerOrder,
            BigDecimal transactionChargePercent,
            BigDecimal fixedAdditionalChargePerOrder) {

        validateNonNegative(
                brokeragePercent,
                "Brokerage percentage"
        );

        validateNonNegative(
                fixedBrokeragePerOrder,
                "Fixed brokerage per order"
        );

        validateNonNegative(
                transactionChargePercent,
                "Transaction charge percentage"
        );

        validateNonNegative(
                fixedAdditionalChargePerOrder,
                "Fixed additional charge per order"
        );

        this.brokeragePercent = brokeragePercent;
        this.fixedBrokeragePerOrder =
                fixedBrokeragePerOrder;
        this.transactionChargePercent =
                transactionChargePercent;
        this.fixedAdditionalChargePerOrder =
                fixedAdditionalChargePerOrder;
    }

    public BigDecimal calculateOrderValue(
            BigDecimal executionPrice,
            long quantity) {

        if (executionPrice == null ||
                executionPrice.signum() <= 0) {

            throw new IllegalArgumentException(
                    "Execution price must be positive"
            );
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        return executionPrice.multiply(
                BigDecimal.valueOf(quantity)
        );
    }

    public BigDecimal calculateBrokerage(
            BigDecimal executionPrice,
            long quantity) {

        BigDecimal orderValue =
                calculateOrderValue(
                        executionPrice,
                        quantity
                );

        BigDecimal percentageCharge =
                calculatePercentageCharge(
                        orderValue,
                        brokeragePercent
                );

        return percentageCharge.add(
                fixedBrokeragePerOrder
        );
    }

    public BigDecimal calculateTransactionCharge(
            BigDecimal executionPrice,
            long quantity) {

        BigDecimal orderValue =
                calculateOrderValue(
                        executionPrice,
                        quantity
                );

        return calculatePercentageCharge(
                orderValue,
                transactionChargePercent
        );
    }

    public BigDecimal calculateAdditionalCharge(
            BigDecimal executionPrice,
            long quantity) {

        calculateOrderValue(
                executionPrice,
                quantity
        );

        return fixedAdditionalChargePerOrder;
    }

    public BigDecimal calculateOrderCost(
            BigDecimal executionPrice,
            long quantity) {

        return calculateBrokerage(
                executionPrice,
                quantity
        )
                .add(
                        calculateTransactionCharge(
                                executionPrice,
                                quantity
                        )
                )
                .add(
                        calculateAdditionalCharge(
                                executionPrice,
                                quantity
                        )
                );
    }

    public BigDecimal calculateRoundTripCost(
            BigDecimal entryExecutionPrice,
            BigDecimal exitExecutionPrice,
            long quantity) {

        BigDecimal entryCost =
                calculateOrderCost(
                        entryExecutionPrice,
                        quantity
                );

        BigDecimal exitCost =
                calculateOrderCost(
                        exitExecutionPrice,
                        quantity
                );

        return entryCost.add(exitCost);
    }

    private BigDecimal calculatePercentageCharge(
            BigDecimal orderValue,
            BigDecimal percentage) {

        return orderValue
                .multiply(percentage)
                .divide(
                        new BigDecimal("100"),
                        10,
                        RoundingMode.HALF_UP
                );
    }

    private void validateNonNegative(
            BigDecimal value,
            String fieldName) {

        if (value == null) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be null"
            );
        }

        if (value.signum() < 0) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be negative"
            );
        }
    }

    public BigDecimal getBrokeragePercent() {
        return brokeragePercent;
    }

    public BigDecimal getFixedBrokeragePerOrder() {
        return fixedBrokeragePerOrder;
    }

    public BigDecimal getTransactionChargePercent() {
        return transactionChargePercent;
    }

    public BigDecimal getFixedAdditionalChargePerOrder() {
        return fixedAdditionalChargePerOrder;
    }
}