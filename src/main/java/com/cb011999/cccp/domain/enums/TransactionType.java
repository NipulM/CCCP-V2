package com.cb011999.cccp.domain.enums;

/**
 * Represents the type of transaction in the SYOS system.
 * Used to distinguish between over-the-counter and online sales.
 */
public enum TransactionType {
    OVER_THE_COUNTER("OTC"),
    ONLINE("ONLINE");
    
    private final String displayName;
    
    TransactionType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}