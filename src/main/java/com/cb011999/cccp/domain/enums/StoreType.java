package com.cb011999.cccp.domain.enums;

/**
 * Represents the type of store in the SYOS system.
 * Physical store uses shelf inventory, online store uses separate web inventory.
 */
public enum StoreType {
    PHYSICAL_STORE("Physical Store"),
    ONLINE_STORE("Online Store");
    
    private final String displayName;
    
    StoreType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}