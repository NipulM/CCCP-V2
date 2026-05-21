package com.cb011999.cccp.service;

import com.cb011999.cccp.domain.enums.StoreType;
import com.cb011999.cccp.domain.model.StockBatch;
import com.cb011999.cccp.observer.StockSubject;

import java.time.LocalDate;
import java.util.List;

/**
 * Interface for inventory operations.
 * 
 * Defines WHAT the inventory service can do, without specifying HOW.
 * This follows the Dependency Inversion Principle from Clean Architecture —
 * servlets and other services depend on this interface, not on a concrete class.
 */
public interface InventoryService {

    StockSubject getStockSubject();

    void addStockToWarehouse(String itemCode, int quantity,
                             LocalDate purchaseDate, LocalDate expiryDate);

    int restockShelves(String itemCode, int neededQty, StoreType storeType);

    boolean reduceStock(String itemCode, int quantity, StoreType storeType);

    int getTotalQuantity(String itemCode, StoreType storeType);

    boolean needsReorder(String itemCode, StoreType storeType);

    List<String> getItemsNeedingReorder(StoreType storeType);

    List<StockBatch> getItemsToReshelf(StoreType storeType, int daysThreshold);

    List<StockBatch> getStockBatches(String itemCode);

    List<StockBatch> getAllWarehouseStock();
}