package com.cb011999.cccp.repository;

import com.cb011999.cccp.domain.model.StockBatch;
import com.cb011999.cccp.domain.enums.StoreType;
import java.util.List;

public interface InventoryRepository {

    void addToStore(StockBatch batch);
    void addToInventory(StockBatch batch, StoreType storeType);
    int getTotalQuantity(String itemCode, StoreType storeType);
    
    List<StockBatch> getStoreStock(String itemCode);
    List<StockBatch> getShelfStock(String itemCode);
    List<StockBatch> getOnlineStock(String itemCode);
    List<StockBatch> getInventory(String itemCode, StoreType storeType);
    List<StockBatch> getAllStoreStock();
    List<StockBatch> getAllShelfStock();
    List<StockBatch> getAllOnlineStock();

    /**
     * Persist a warehouse batch quantity change (e.g. after restock reduces warehouse stock).
     * No-op for in-memory (same object reference); required for database.
     */
    void updateStoreBatch(StockBatch batch);

    /**
     * Persist a shelf/online batch quantity change (e.g. after checkout reduces stock).
     * No-op for in-memory; required for database.
     */
    void updateInventoryBatch(StockBatch batch, StoreType storeType);
}