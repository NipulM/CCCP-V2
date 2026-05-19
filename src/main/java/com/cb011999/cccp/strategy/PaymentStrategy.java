package com.cb011999.cccp.strategy;

import com.cb011999.cccp.domain.enums.PaymentMethod;

public interface PaymentStrategy {
    // Process the payment and return true if successful
    boolean processPayment(double amount);
    String getPaymentDetails();
    /** Which payment method this strategy represents (for bill attribute / display). */
    PaymentMethod getPaymentMethodType();
}