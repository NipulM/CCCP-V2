package com.cb011999.cccp.repository.impl;

import com.cb011999.cccp.domain.model.StockBatch;
import com.cb011999.cccp.domain.enums.StoreType;
import com.cb011999.cccp.repository.InventoryRepository;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory implementation of InventoryRepository.
 * Uses Singleton pattern to ensure single instance.
 * Manages three separate inventories: store, shelf, and online.
 */
public class InMemoryInventoryRepository implements InventoryRepository {
    private static InMemoryInventoryRepository instance;
    
    private final List<StockBatch> storeInventory;    // Warehouse stock
    private final List<StockBatch> shelfInventory;    // Physical store shelf
    private final List<StockBatch> onlineInventory;   // Online store inventory
    
    private InMemoryInventoryRepository() {
        this.storeInventory = new ArrayList<>();
        this.shelfInventory = new ArrayList<>();
        this.onlineInventory = new ArrayList<>();
    }
    
    public static synchronized InMemoryInventoryRepository getInstance() {
        if (instance == null) {
            instance = new InMemoryInventoryRepository();
        }
        return instance;
    }
    
    @Override
    public void addToStore(StockBatch batch) {
        storeInventory.add(batch);
    }
    
    @Override
    public void addToInventory(StockBatch batch, StoreType storeType) {
        if (storeType == StoreType.PHYSICAL_STORE) {
            shelfInventory.add(batch);
        } else {
            onlineInventory.add(batch);
        }
    }
    
    @Override
    public List<StockBatch> getStoreStock(String itemCode) {
        return storeInventory.stream()
            .filter(batch -> batch.getItemCode().equals(itemCode))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<StockBatch> getShelfStock(String itemCode) {
        return shelfInventory.stream()
            .filter(batch -> batch.getItemCode().equals(itemCode))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<StockBatch> getOnlineStock(String itemCode) {
        return onlineInventory.stream()
            .filter(batch -> batch.getItemCode().equals(itemCode))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<StockBatch> getInventory(String itemCode, StoreType storeType) {
        if (storeType == StoreType.PHYSICAL_STORE) {
            return getShelfStock(itemCode);
        } else {
            return getOnlineStock(itemCode);
        }
    }
    
    @Override
    public List<StockBatch> getAllStoreStock() {
        return new ArrayList<>(storeInventory);
    }
    
    @Override
    public List<StockBatch> getAllShelfStock() {
        return new ArrayList<>(shelfInventory);
    }
    
    @Override
    public List<StockBatch> getAllOnlineStock() {
        return new ArrayList<>(onlineInventory);
    }
    
    @Override
    public int getTotalQuantity(String itemCode, StoreType storeType) {
        return getInventory(itemCode, storeType).stream()
            .mapToInt(StockBatch::getQuantity)
            .sum();
    }
    
    @Override
    public void updateStoreBatch(StockBatch batch) {
        // In-memory: batches in list are same references; quantity already updated
        // No-op
    }

    @Override
    public void updateInventoryBatch(StockBatch batch, StoreType storeType) {
        // In-memory: batches in list are same references; quantity already updated
        // No-op
    }

    /**
     * Clears all inventories (useful for testing).
     */
    public void clear() {
        storeInventory.clear();
        shelfInventory.clear();
        onlineInventory.clear();
    }
}