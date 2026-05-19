package com.cb011999.cccp.observer;

import com.cb011999.cccp.domain.enums.StoreType;

import java.util.ArrayList;
import java.util.List;

public class StockSubject {
    private final List<StockObserver> observers;
    private static final int REORDER_THRESHOLD = 50;
    
    public StockSubject() {
        this.observers = new ArrayList<>();
    }
    
    public void attach(StockObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }
    
    public void detach(StockObserver observer) {
        observers.remove(observer);
    }
    
    public void notifyStockChanged(String itemCode, int currentQuantity, StoreType storeType) {
        // Notify all observers
        for (StockObserver observer : observers) {
            observer.onStockChanged(itemCode, currentQuantity, storeType);
            
            // Check for low stock
            if (currentQuantity > 0 && currentQuantity < REORDER_THRESHOLD) {
                observer.onLowStock(itemCode, currentQuantity, storeType);
            }
            
            // Check for depleted stock
            if (currentQuantity == 0) {
                observer.onStockDepleted(itemCode, storeType);
            }
        }
    }
    
    public int getObserverCount() {
        return observers.size();
    }
}