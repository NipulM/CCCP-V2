package com.cb011999.cccp.domain.enums;

/**
 * How the bill was paid. Used as an attribute on the bill for display and reporting.
 */
public enum PaymentMethod {
    CASH("Cash"),
    CARD("Card"),
    ONLINE("Online");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
