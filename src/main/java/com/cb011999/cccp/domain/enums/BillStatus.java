package com.cb011999.cccp.domain.enums;

/**
 * Represents the status of a bill in the system.
 */
public enum BillStatus {
    PENDING("Pending"),
    PAID("Paid"),
    CANCELLED("Cancelled");
    
    private final String displayName;
    
    BillStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}