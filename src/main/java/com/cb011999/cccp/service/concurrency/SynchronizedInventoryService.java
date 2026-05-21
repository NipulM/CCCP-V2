package com.cb011999.cccp.service.concurrency;


import com.cb011999.cccp.domain.enums.StoreType;
import com.cb011999.cccp.domain.model.StockBatch;
import com.cb011999.cccp.observer.StockSubject;
import com.cb011999.cccp.service.InventoryService;
import com.cb011999.cccp.service.impl.InventoryServiceImpl;
import com.cb011999.cccp.web.concurrency.RequestQueue;
import com.cb011999.cccp.web.concurrency.Task;

import java.time.LocalDate;
import java.util.List;

/**
 * Thread-safe implementation of InventoryService.
 * 
 * Wraps InventoryServiceImpl and routes all write operations through
 * the SYOSRequestQueue. This ensures that when multiple users (e.g.,
 * two cashiers, or one cashier and one online customer) modify stock
 * at the same time, their operations are queued and processed one
 * at a time — preventing race conditions.
 * 
 * Read operations use synchronized blocks for consistent reads but
 * don't go through the queue (they don't modify data, so they're safe
 * to run alongside the worker thread as long as reads are atomic).
 * 
 * This class implements the same InventoryService interface as the Impl,
 * so servlets don't know or care which version they're using.
 * That's the power of programming to an interface.
 */
public class SynchronizedInventoryService implements InventoryService {

    private final InventoryServiceImpl delegate;
    private final RequestQueue requestQueue;
    private final Object stockLock = new Object();

    public SynchronizedInventoryService(InventoryServiceImpl delegate) {
        this.delegate = delegate;
        this.requestQueue = RequestQueue.getInstance();
    }

    @Override
    public StockSubject getStockSubject() {
        return delegate.getStockSubject();
    }

    @Override
    public void addStockToWarehouse(String itemCode, int quantity,
                                    LocalDate purchaseDate, LocalDate expiryDate) {
        Task task = new Task(() -> {
            synchronized (stockLock) {
                delegate.addStockToWarehouse(itemCode, quantity, purchaseDate, expiryDate);
            }
        });
        requestQueue.submitTask(task);
    }
    
    @Override
    public int restockShelves(String itemCode, int neededQty, StoreType storeType) {
        final Task[] holder = new Task[1];
        holder[0] = new Task(() -> {
            synchronized (stockLock) {
                int moved = delegate.restockShelves(itemCode, neededQty, storeType);
                holder[0].setResult(moved);
            }
        });
        requestQueue.submitTask(holder[0]);

        if (holder[0].hasError()) return 0;
        Object result = holder[0].getResult();
        return result != null ? (int) result : 0;
    }

    @Override
    public boolean reduceStock(String itemCode, int quantity, StoreType storeType) {
        final Task[] holder = new Task[1];
        holder[0] = new Task(() -> {
            synchronized (stockLock) {
                boolean success = delegate.reduceStock(itemCode, quantity, storeType);
                holder[0].setResult(success);
            }
        });
        requestQueue.submitTask(holder[0]);

        if (holder[0].hasError()) return false;
        Object result = holder[0].getResult();
        return result != null ? (boolean) result : false;
    }

    @Override
    public int getTotalQuantity(String itemCode, StoreType storeType) {
        // Read operation — synchronized for consistency but not queued
        synchronized (stockLock) {
            return delegate.getTotalQuantity(itemCode, storeType);
        }
    }

    @Override
    public boolean needsReorder(String itemCode, StoreType storeType) {
        synchronized (stockLock) {
            return delegate.needsReorder(itemCode, storeType);
        }
    }

    @Override
    public List<String> getItemsNeedingReorder(StoreType storeType) {
        synchronized (stockLock) {
            return delegate.getItemsNeedingReorder(storeType);
        }
    }

    @Override
    public List<StockBatch> getItemsToReshelf(StoreType storeType, int daysThreshold) {
        synchronized (stockLock) {
            return delegate.getItemsToReshelf(storeType, daysThreshold);
        }
    }

    @Override
    public List<StockBatch> getStockBatches(String itemCode) {
        synchronized (stockLock) {
            return delegate.getStockBatches(itemCode);
        }
    }

    @Override
    public List<StockBatch> getAllWarehouseStock() {
        synchronized (stockLock) {
            return delegate.getAllWarehouseStock();
        }
    }
}