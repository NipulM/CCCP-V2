package com.cb011999.cccp.observer;

import com.cb011999.cccp.domain.enums.StoreType;

public interface StockObserver {

    void onStockChanged(String itemCode, int currentQuantity, StoreType storeType);
    void onLowStock(String itemCode, int currentQuantity, StoreType storeType);
    void onStockDepleted(String itemCode, StoreType storeType);
}