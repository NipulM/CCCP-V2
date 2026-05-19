package com.cb011999.cccp.service;

import com.cb011999.cccp.domain.enums.StoreType;
import com.cb011999.cccp.domain.model.StockBatch;
import com.cb011999.cccp.observer.StockSubject;
import com.cb011999.cccp.repository.InventoryRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class InventoryService {
    private static final int REORDER_THRESHOLD = 50;
    
    private final InventoryRepository inventoryRepository;
    private final StockSubject stockSubject;
    
    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
        this.stockSubject = new StockSubject();
    }
    
    public StockSubject getStockSubject() {
        return stockSubject;
    }
    
    public void addStockToWarehouse(String itemCode, int quantity, 
                                    LocalDate purchaseDate, LocalDate expiryDate) {
        StockBatch batch = new StockBatch(itemCode, quantity, purchaseDate, expiryDate);
        inventoryRepository.addToStore(batch);
    }
    
    public int restockShelves(String itemCode, int neededQty, StoreType storeType) {
        // Get available batches from warehouse
        List<StockBatch> availableBatches = inventoryRepository.getStoreStock(itemCode)
            .stream()
            .filter(StockBatch::hasStock)
            .sorted(new SmartBatchComparator())
            .collect(Collectors.toList());
        
        int totalMoved = 0;
        
        for (StockBatch sourceBatch : availableBatches) {
            if (totalMoved >= neededQty) {
                break;
            }
            
            int toMove = Math.min(neededQty - totalMoved, sourceBatch.getQuantity());
            
            // Reduce from warehouse
            sourceBatch.reduceStock(toMove);
            inventoryRepository.updateStoreBatch(sourceBatch);
            
            // Add to shelf/online inventory
            StockBatch shelfBatch = new StockBatch(
                itemCode, 
                toMove, 
                sourceBatch.getPurchaseDate(), 
                sourceBatch.getExpiryDate()
            );
            inventoryRepository.addToInventory(shelfBatch, storeType);
            
            totalMoved += toMove;
        }
        
        return totalMoved;
    }
    
    public boolean reduceStock(String itemCode, int quantity, StoreType storeType) {
        // Check if enough stock is available
        int available = getTotalQuantity(itemCode, storeType);
        if (available < quantity) {
            return false;
        }
        
        List<StockBatch> batches = inventoryRepository.getInventory(itemCode, storeType)
            .stream()
            .filter(StockBatch::hasStock)
            .sorted(Comparator.comparing(StockBatch::getExpiryDate))
            .collect(Collectors.toList());
        
        int remaining = quantity;
        
        for (StockBatch batch : batches) {
            if (remaining <= 0) {
                break;
            }
            
            int toReduce = Math.min(remaining, batch.getQuantity());
            batch.reduceStock(toReduce);
            inventoryRepository.updateInventoryBatch(batch, storeType);
            remaining -= toReduce;
        }
        
        // Notify observers of stock change
        int newQuantity = getTotalQuantity(itemCode, storeType);
        stockSubject.notifyStockChanged(itemCode, newQuantity, storeType);
        
        return true;
    }

    public int getTotalQuantity(String itemCode, StoreType storeType) {
        return inventoryRepository.getTotalQuantity(itemCode, storeType);
    }
    
    public boolean needsReorder(String itemCode, StoreType storeType) {
        return getTotalQuantity(itemCode, storeType) < REORDER_THRESHOLD;
    }
    
    public List<String> getItemsNeedingReorder(StoreType storeType) {
        List<StockBatch> allStock = storeType == StoreType.PHYSICAL_STORE 
            ? inventoryRepository.getAllShelfStock() 
            : inventoryRepository.getAllOnlineStock();
        
        return allStock.stream()
            .collect(Collectors.groupingBy(
                StockBatch::getItemCode,
                Collectors.summingInt(StockBatch::getQuantity)
            ))
            .entrySet().stream()
            .filter(entry -> entry.getValue() < REORDER_THRESHOLD)
            .map(entry -> entry.getKey())
            .collect(Collectors.toList());
    }
    
    public List<StockBatch> getItemsToReshelf(StoreType storeType, int daysThreshold) {
        List<StockBatch> inventory = storeType == StoreType.PHYSICAL_STORE
            ? inventoryRepository.getAllShelfStock()
            : inventoryRepository.getAllOnlineStock();
        
        return inventory.stream()
            .filter(batch -> batch.getDaysUntilExpiry() <= daysThreshold)
            .collect(Collectors.toList());
    }
    
    public List<StockBatch> getStockBatches(String itemCode) {
        return inventoryRepository.getStoreStock(itemCode);
    }

    public List<StockBatch> getAllWarehouseStock() {
        return inventoryRepository.getAllStoreStock();
    }
    

    private static class SmartBatchComparator implements Comparator<StockBatch> {
        @Override
        public int compare(StockBatch b1, StockBatch b2) {
            // First compare by expiry date (closer expiry = higher priority)
            int expiryCompare = b1.getExpiryDate().compareTo(b2.getExpiryDate());
            if (expiryCompare != 0) {
                return expiryCompare;
            }
            // If same expiry, use older batch (FIFO)
            return b1.getPurchaseDate().compareTo(b2.getPurchaseDate());
        }
    }
}