package com.cb011999.cccp.strategy;

import com.cb011999.cccp.domain.enums.PaymentMethod;

public class CashPayment implements PaymentStrategy {
    private double amountTendered;
    private double change;

    public CashPayment(double amountTendered, double totalBill) {
        this.amountTendered = amountTendered;
        this.change = amountTendered - totalBill;
    }

    @Override
    public boolean processPayment(double amount) {
        return amountTendered >= amount;
    }

    @Override
    public String getPaymentDetails() {
        return String.format("Cash Tendered: %.2f, Change: %.2f", amountTendered, change);
    }

    @Override
    public PaymentMethod getPaymentMethodType() {
        return PaymentMethod.CASH;
    }
}
