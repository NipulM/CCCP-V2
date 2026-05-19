package com.cb011999.cccp.observer;

import com.cb011999.cccp.domain.enums.StoreType;

public class ConsoleStockObserver implements StockObserver {
    
    @Override
    public void onStockChanged(String itemCode, int currentQuantity, StoreType storeType) {
        System.out.println(String.format(
            "📦 Stock Update: %s in %s now has %d units",
            itemCode, storeType.getDisplayName(), currentQuantity
        ));
    }
    
    @Override
    public void onLowStock(String itemCode, int currentQuantity, StoreType storeType) {
        System.out.println(String.format(
            "⚠️  LOW STOCK ALERT: %s in %s has only %d units left! (Threshold: 50)",
            itemCode, storeType.getDisplayName(), currentQuantity
        ));
    }
    
    @Override
    public void onStockDepleted(String itemCode, StoreType storeType) {
        System.out.println(String.format(
            "🚨 OUT OF STOCK: %s in %s is completely depleted!",
            itemCode, storeType.getDisplayName()
        ));
    }
}